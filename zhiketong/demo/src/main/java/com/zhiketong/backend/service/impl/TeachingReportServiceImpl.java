package com.zhiketong.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiketong.backend.config.DeepSeekProperties;
import com.zhiketong.backend.dto.KnowledgePointStatVO;
import com.zhiketong.backend.dto.TeachingReportRequestDTO;
import com.zhiketong.backend.dto.TeachingReportVO;
import com.zhiketong.backend.mapper.UserAnswerMapper;
import com.zhiketong.backend.service.TeachingReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TeachingReportServiceImpl implements TeachingReportService {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    @Resource
    private DeepSeekProperties deepSeekProperties;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public TeachingReportVO generate(TeachingReportRequestDTO dto) {
        List<KnowledgePointStatVO> stats = userAnswerMapper.selectKnowledgePointStats(
                dto.getStudentIds(),
                dto.getKnowledgePointIds(),
                dto.getClassName()
        );

        TeachingReportVO report = new TeachingReportVO();
        report.setTitle("AI教研报告");
        report.setClassName(dto.getClassName());
        report.setGeneratedAt(LocalDateTime.now());
        report.setKnowledgePointStats(stats);
        report.setSummary(buildSummary(stats));
        report.setSuggestions(buildTemplateSuggestions(stats));
        report.setGeneratedByAi(false);
        report.setAiAnalysis("当前未配置 DeepSeek API Key，返回固定教研模板。");

        if (Boolean.TRUE.equals(dto.getUseAI()) && hasText(deepSeekProperties.getApiKey())) {
            try {
                String aiText = callDeepSeek(report, dto);
                if (hasText(aiText)) {
                    report.setGeneratedByAi(true);
                    report.setAiAnalysis(aiText);
                }
            } catch (Exception e) {
                report.setAiAnalysis("DeepSeek 调用失败，已返回本地模板分析。");
            }
        }
        return report;
    }

    private String buildSummary(List<KnowledgePointStatVO> stats) {
        if (stats == null || stats.isEmpty()) {
            return "暂无练习数据，建议先组织一次基础诊断练习。";
        }
        KnowledgePointStatVO weakest = stats.get(0);
        return "共统计 " + stats.size() + " 个知识点，当前薄弱点集中在「"
                + weakest.getKnowledgePointName() + "」，正确率约 "
                + Math.round(defaultDouble(weakest.getAccuracyRate()) * 100) + "%。";
    }

    private List<String> buildTemplateSuggestions(List<KnowledgePointStatVO> stats) {
        List<String> suggestions = new ArrayList<>();
        if (stats == null || stats.isEmpty()) {
            suggestions.add("先发布一次覆盖核心知识点的诊断练习，形成学生薄弱点数据。");
            suggestions.add("后续根据错题分布推送分层练习。");
            return suggestions;
        }
        suggestions.add("对正确率低于 60% 的知识点安排讲评和二次练习。");
        suggestions.add("对中等掌握学生推送 medium 难度巩固题，对高掌握学生推送 hard 拓展题。");
        suggestions.add("将高频错题同步到错题本，下一次课前 10 分钟复测。");
        return suggestions;
    }

    private String callDeepSeek(TeachingReportVO report, TeachingReportRequestDTO dto) throws Exception {
        String statsJson = objectMapper.writeValueAsString(report.getKnowledgePointStats());
        String prompt = "请基于以下班级练习统计生成一份中文教研报告，包含总体表现、薄弱知识点、分层教学建议和下次作业建议。"
                + "班级：" + (dto.getClassName() == null ? "未指定" : dto.getClassName())
                + "；知识点统计：" + statsJson;

        Map<String, Object> body = Map.of(
                "model", deepSeekProperties.getModel(),
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", "你是经验丰富的中小学教研分析助手，输出要结构化、可执行。"),
                        Map.of("role", "user", "content", prompt)
                )
        );

        RestClient client = RestClient.builder()
                .baseUrl(deepSeekProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
        String response = client.post()
                .uri("/chat/completions")
                .body(body)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);
        return root.path("choices").path(0).path("message").path("content").asText();
    }

    private double defaultDouble(Double value) {
        return value == null ? 0 : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
