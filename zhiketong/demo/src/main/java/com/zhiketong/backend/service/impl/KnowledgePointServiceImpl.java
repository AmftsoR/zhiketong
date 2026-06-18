package com.zhiketong.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiketong.backend.common.BusinessException;
import com.zhiketong.backend.config.DeepSeekProperties;
import com.zhiketong.backend.dto.KnowledgePointCreateDTO;
import com.zhiketong.backend.dto.KnowledgePointGenerateDTO;
import com.zhiketong.backend.dto.KnowledgePointMoveDTO;
import com.zhiketong.backend.dto.KnowledgePointTreeVO;
import com.zhiketong.backend.dto.KnowledgePointUpdateDTO;
import com.zhiketong.backend.entity.KnowledgePoint;
import com.zhiketong.backend.mapper.KnowledgePointMapper;
import com.zhiketong.backend.service.KnowledgePointService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KnowledgePointServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint>
        implements KnowledgePointService {

    @Resource
    private DeepSeekProperties deepSeekProperties;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public List<KnowledgePointTreeVO> getTree() {
        List<KnowledgePoint> all = baseMapper.selectAllActive();
        Map<Long, List<KnowledgePoint>> grouped = all.stream()
                .collect(Collectors.groupingBy(k -> k.getParentId() == null ? 0L : k.getParentId()));
        return buildTree(grouped, 0L);
    }

    private List<KnowledgePointTreeVO> buildTree(Map<Long, List<KnowledgePoint>> grouped, Long parentId) {
        List<KnowledgePoint> list = grouped.get(parentId);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream().map(kp -> {
            KnowledgePointTreeVO vo = BeanUtil.copyProperties(kp, KnowledgePointTreeVO.class);
            vo.setChildren(buildTree(grouped, kp.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public KnowledgePoint create(KnowledgePointCreateDTO dto) {
        KnowledgePoint entity = BeanUtil.copyProperties(dto, KnowledgePoint.class);
        save(entity);
        return entity;
    }

    @Override
    @Transactional
    public KnowledgePoint update(KnowledgePointUpdateDTO dto) {
        KnowledgePoint entity = getById(dto.getId());
        if (entity == null) {
            throw new BusinessException("知识点不存在");
        }
        BeanUtil.copyProperties(dto, entity, "id");
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        long childCount = count(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该节点下存在子节点，无法删除");
        }
        removeById(id);
    }

    @Override
    @Transactional
    public void move(Long id, KnowledgePointMoveDTO dto) {
        KnowledgePoint entity = getById(id);
        if (entity == null) {
            throw new BusinessException("知识点不存在");
        }
        entity.setParentId(dto.getNewParentId());
        updateById(entity);
    }

    @Override
    public List<KnowledgePoint> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return listByIds(ids);
    }

    @Override
    @Transactional
    public List<KnowledgePointTreeVO> generate(KnowledgePointGenerateDTO dto) {
        if (!hasText(deepSeekProperties.getApiKey())) {
            throw new BusinessException("未配置 DeepSeek API Key");
        }
        String subject = dto.getSubject();
        String topic = dto.getTopic() != null ? dto.getTopic() : subject;
        String prompt = String.format(
            "请为「%s」学科生成一个知识点树（JSON格式）。主题：%s。最多3层，5-10个知识点。" +
            "每个节点包含 name(知识点名称) 和 children(子节点数组)。" +
            "严格返回以下JSON格式，不要包含任何其他文字：" +
            "[{\"name\":\"父知识点\",\"children\":[{\"name\":\"子知识点\",\"children\":[]}]}]",
            subject, topic);

        try {
            Map<String, Object> body = Map.of(
                "model", deepSeekProperties.getModel(),
                "stream", false,
                "messages", List.of(
                    Map.of("role", "system", "content", "你是课程大纲设计专家，输出必须是纯JSON数组。"),
                    Map.of("role", "user", "content", prompt)
                )
            );
            RestClient client = RestClient.builder()
                .baseUrl(deepSeekProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
            String response = client.post().uri("/chat/completions").body(body).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            // 提取JSON部分
            String json = content.trim();
            int start = json.indexOf('[');
            int end = json.lastIndexOf(']');
            if (start >= 0 && end > start) json = json.substring(start, end + 1);

            JsonNode arr = objectMapper.readTree(json);
            saveTree(arr, null, dto.getSubject());
            return getTree();
        } catch (BusinessException e) { throw e;
        } catch (Exception e) { throw new BusinessException("AI 生成失败: " + e.getMessage()); }
    }

    private void saveTree(JsonNode nodes, Long parentId, String subject) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            String name = node.path("name").asText();
            if (name.isBlank()) continue;
            KnowledgePoint kp = new KnowledgePoint();
            kp.setName(name);
            kp.setParentId(parentId);
            kp.setSubject(subject);
            save(kp);
            JsonNode children = node.path("children");
            if (children.isArray() && !children.isEmpty()) {
                saveTree(children, kp.getId(), subject);
            }
        }
    }

    private boolean hasText(String s) { return s != null && !s.trim().isEmpty(); }
}
