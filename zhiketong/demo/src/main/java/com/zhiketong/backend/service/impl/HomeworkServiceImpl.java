package com.zhiketong.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiketong.backend.common.BusinessException;
import com.zhiketong.backend.dto.HomeworkCreateDTO;
import com.zhiketong.backend.dto.HomeworkQuery;
import com.zhiketong.backend.dto.HomeworkVO;
import com.zhiketong.backend.entity.ClassEntity;
import com.zhiketong.backend.entity.Homework;
import com.zhiketong.backend.entity.HomeworkTarget;
import com.zhiketong.backend.entity.Question;
import com.zhiketong.backend.mapper.ClassMapper;
import com.zhiketong.backend.mapper.HomeworkMapper;
import com.zhiketong.backend.mapper.HomeworkTargetMapper;
import com.zhiketong.backend.mapper.QuestionMapper;
import com.zhiketong.backend.service.HomeworkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeworkServiceImpl implements HomeworkService {

    @Resource
    private HomeworkMapper homeworkMapper;

    @Resource
    private HomeworkTargetMapper homeworkTargetMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private ClassMapper classMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public HomeworkVO create(HomeworkCreateDTO dto) {
        validateCreate(dto);

        // 解析班级：classId 优先
        String className = resolveClassName(dto);

        // 确定题目列表：questionIds > 自动从题库按知识点+难度抽取
        List<Long> questionIds = dto.getQuestionIds();
        List<Question> pickedQuestions = null;
        if (questionIds == null || questionIds.isEmpty()) {
            pickedQuestions = pickQuestionsFromBank(dto);
            questionIds = pickedQuestions.stream().map(Question::getId).collect(Collectors.toList());
        }

        Homework homework = new Homework();
        homework.setTeacherId(dto.getTeacherId());
        homework.setTitle(dto.getTitle());
        homework.setDescription(dto.getDescription());
        homework.setKnowledgePointIds(writeJson(dto.getKnowledgePointIds()));
        homework.setQuestionIds(writeJson(questionIds));
        homework.setDifficulty(dto.getDifficulty());
        homework.setClassId(dto.getClassId());
        homework.setClassName(className);
        homework.setStatus("published");
        homework.setDeadline(dto.getDeadline());
        homeworkMapper.insert(homework);

        // 创建推送目标
        List<HomeworkTarget> targets = new ArrayList<>();
        if (dto.getClassId() != null) {
            HomeworkTarget t = createTarget(homework.getId(), "class", dto.getClassId(), className);
            targets.add(t);
        } else if (hasText(dto.getClassName())) {
            HomeworkTarget t = createTarget(homework.getId(), "class", null, dto.getClassName());
            targets.add(t);
        }
        if (dto.getStudentIds() != null) {
            for (Long sid : dto.getStudentIds()) {
                if (sid != null) {
                    targets.add(createTarget(homework.getId(), "student", sid, null));
                }
            }
        }
        for (HomeworkTarget t : targets) {
            homeworkTargetMapper.insert(t);
        }

        // 组装VO，含题目详情
        if (pickedQuestions == null) {
            pickedQuestions = questionMapper.selectBatchIds(questionIds);
        }
        return toVO(homework, targets, pickedQuestions);
    }

    @Override
    public HomeworkVO getDetail(Long id) {
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }
        List<HomeworkTarget> targets = homeworkTargetMapper.selectList(
                new LambdaQueryWrapper<HomeworkTarget>()
                        .eq(HomeworkTarget::getHomeworkId, id));
        List<Long> qIds = readJson(homework.getQuestionIds());
        List<Question> questions = qIds.isEmpty()
                ? new ArrayList<>()
                : questionMapper.selectBatchIds(qIds);
        return toVO(homework, targets, questions);
    }

    @Override
    public Page<Homework> page(HomeworkQuery query) {
        Page<Homework> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<Homework>()
                .eq(query.getTeacherId() != null, Homework::getTeacherId, query.getTeacherId())
                .eq(hasText(query.getClassName()), Homework::getClassName, query.getClassName())
                .eq(hasText(query.getDifficulty()), Homework::getDifficulty, query.getDifficulty())
                .orderByDesc(Homework::getCreatedAt);
        return homeworkMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public HomeworkVO publish(Long id) {
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }
        homework.setStatus("published");
        homeworkMapper.updateById(homework);
        return getDetail(id);
    }

    @Override
    public List<HomeworkVO> getByClassName(String className) {
        List<Homework> list = homeworkMapper.selectList(
                new LambdaQueryWrapper<Homework>()
                        .eq(Homework::getClassName, className)
                        .eq(Homework::getStatus, "published")
                        .orderByDesc(Homework::getCreatedAt));
        List<HomeworkVO> result = new ArrayList<>();
        for (Homework h : list) {
            List<HomeworkTarget> targets = homeworkTargetMapper.selectList(
                    new LambdaQueryWrapper<HomeworkTarget>().eq(HomeworkTarget::getHomeworkId, h.getId()));
            List<Long> qIds = readJson(h.getQuestionIds());
            List<Question> questions = qIds.isEmpty() ? new ArrayList<>() : questionMapper.selectBatchIds(qIds);
            result.add(toVO(h, targets, questions));
        }
        return result;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        homeworkTargetMapper.delete(new LambdaQueryWrapper<HomeworkTarget>()
                .eq(HomeworkTarget::getHomeworkId, id));
        homeworkMapper.deleteById(id);
    }

    // ============ 从题库自动抽题 ============

    private List<Question> pickQuestionsFromBank(HomeworkCreateDTO dto) {
        List<Long> kpIds = dto.getKnowledgePointIds();
        String diff = dto.getDifficulty();
        int total = (dto.getTotalCount() != null && dto.getTotalCount() > 0) ? dto.getTotalCount() : 10;

        Map<String, Integer> distribution;
        if ("mixed".equals(diff)) {
            distribution = buildMixedDistribution(dto, total);
        } else {
            distribution = Map.of(diff, total);
        }

        List<Question> result = new ArrayList<>();
        List<Long> excludeIds = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            int need = entry.getValue();
            if (need <= 0) continue;
            List<Question> picked = questionMapper.selectByKnowledgePointsAndDifficulty(
                    kpIds, entry.getKey(), excludeIds, need);
            result.addAll(picked);
            picked.forEach(q -> excludeIds.add(q.getId()));
        }

        int missing = total - result.size();
        if (missing > 0) {
            List<Question> fallback = questionMapper.selectByKnowledgePointsAndDifficulty(
                    kpIds, null, excludeIds, missing);
            result.addAll(fallback);
        }

        if (result.isEmpty()) {
            throw new BusinessException("题库中没有匹配的题目，请检查知识点和难度设置");
        }
        return result;
    }

    private Map<String, Integer> buildMixedDistribution(HomeworkCreateDTO dto, int total) {
        int easy = nvl(dto.getEasyCount(), Math.max(1, Math.round(total * 0.5f)));
        int medium = nvl(dto.getMediumCount(), Math.max(0, Math.round(total * 0.3f)));
        int hard = nvl(dto.getHardCount(), total - easy - medium);
        if (hard < 0) hard = 0;
        // 溢出调整
        int sum = easy + medium + hard;
        if (sum < total) medium += total - sum;
        else if (sum > total) {
            int overflow = sum - total;
            hard = Math.max(0, hard - Math.min(hard, overflow));
            overflow = sum - total - (sum - total - Math.min(0, overflow)); // 重新算
            medium = Math.max(0, medium - overflow);
        }
        Map<String, Integer> dist = new LinkedHashMap<>();
        dist.put("easy", easy);
        dist.put("medium", medium);
        dist.put("hard", hard);
        return dist;
    }

    // ============ 班级名称解析 ============

    private String resolveClassName(HomeworkCreateDTO dto) {
        if (dto.getClassId() != null) {
            ClassEntity cls = classMapper.selectById(dto.getClassId());
            if (cls != null) {
                return cls.getName();
            }
            throw new BusinessException("班级不存在: id=" + dto.getClassId());
        }
        return dto.getClassName(); // 兼容手动输入
    }

    // ============ 校验 ============

    private void validateCreate(HomeworkCreateDTO dto) {
        if (dto.getTeacherId() == null) {
            throw new BusinessException("教师ID不能为空");
        }
        if (!hasText(dto.getTitle())) {
            throw new BusinessException("作业标题不能为空");
        }
        if (dto.getKnowledgePointIds() == null || dto.getKnowledgePointIds().isEmpty()) {
            throw new BusinessException("知识点不能为空");
        }
        if (!hasText(dto.getDifficulty())) {
            throw new BusinessException("作业难度不能为空");
        }
        boolean hasClass = dto.getClassId() != null || hasText(dto.getClassName());
        boolean hasStudents = dto.getStudentIds() != null && !dto.getStudentIds().isEmpty();
        if (!hasClass && !hasStudents) {
            throw new BusinessException("请至少指定班级或学生");
        }
    }

    // ============ 组装 VO ============

    private HomeworkVO toVO(Homework h, List<HomeworkTarget> targets, List<Question> questions) {
        HomeworkVO vo = new HomeworkVO();
        vo.setId(h.getId());
        vo.setTeacherId(h.getTeacherId());
        vo.setTitle(h.getTitle());
        vo.setDescription(h.getDescription());
        vo.setDifficulty(h.getDifficulty());
        vo.setClassId(h.getClassId());
        vo.setClassName(h.getClassName());
        vo.setStatus(h.getStatus());
        vo.setDeadline(h.getDeadline());
        vo.setCreatedAt(h.getCreatedAt());
        vo.setKnowledgePointIds(readJson(h.getKnowledgePointIds()));
        vo.setQuestions(questions != null ? questions : new ArrayList<>());
        vo.setTargets(targets);

        // 统计各难度题目数量
        if (questions != null) {
            int ec = 0, mc = 0, hc = 0;
            for (Question q : questions) {
                String d = q.getDifficulty();
                if ("easy".equals(d)) ec++;
                else if ("hard".equals(d)) hc++;
                else mc++;
            }
            vo.setEasyCount(ec);
            vo.setMediumCount(mc);
            vo.setHardCount(hc);
        }
        return vo;
    }

    // ============ 工具方法 ============

    private HomeworkTarget createTarget(Long homeworkId, String targetType, Long targetId, String className) {
        HomeworkTarget t = new HomeworkTarget();
        t.setHomeworkId(homeworkId);
        t.setTargetType(targetType);
        t.setTargetId(targetId);
        t.setClassName(className);
        return t;
    }

    private String writeJson(List<Long> ids) {
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (Exception e) {
            throw new BusinessException("JSON序列化失败");
        }
    }

    private List<Long> readJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int nvl(Integer val, int def) {
        return (val != null && val > 0) ? val : def;
    }
}
