package com.zhiketong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiketong.entity.KnowledgePoint;
import com.zhiketong.entity.QuestionBank;
import com.zhiketong.entity.UserAnswer;
import com.zhiketong.mapper.KnowledgePointMapper;
import com.zhiketong.mapper.QuestionBankMapper;
import com.zhiketong.mapper.UserAnswerMapper;
import com.zhiketong.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoryServiceImpl implements HistoryService {

    @Autowired
    private UserAnswerMapper userAnswerMapper;
    @Autowired
    private QuestionBankMapper questionBankMapper;
    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Override
    public Map<String, Object> getHistory(Long userId, String subject, String startDate, String endDate) {
        // 查询用户所有答题记录，按时间倒序
        LambdaQueryWrapper<UserAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswer::getUserId, userId)
               .orderByDesc(UserAnswer::getAnsweredAt);
        List<UserAnswer> answers = userAnswerMapper.selectList(wrapper);

        // 批量查询题目信息
        List<Long> questionIds = answers.stream()
                .map(UserAnswer::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, QuestionBank> questionMap = new HashMap<>();
        Map<Long, KnowledgePoint> kpMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            List<QuestionBank> questions = questionBankMapper.selectBatchIds(questionIds);
            questionMap = questions.stream()
                    .collect(Collectors.toMap(QuestionBank::getId, q -> q));

            // 批量查询知识点
            List<Long> kpIds = questions.stream()
                    .map(QuestionBank::getKnowledgePointId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!kpIds.isEmpty()) {
                List<KnowledgePoint> kps = knowledgePointMapper.selectBatchIds(kpIds);
                kpMap = kps.stream().collect(Collectors.toMap(KnowledgePoint::getId, k -> k));
            }
        }

        // 日期过滤（如果指定）
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = (startDate != null && !startDate.isEmpty())
                ? LocalDate.parse(startDate, fmt).atStartOfDay() : null;
        LocalDateTime end = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate, fmt).plusDays(1).atStartOfDay() : null;

        // 组装记录列表
        List<Map<String, Object>> records = new ArrayList<>();
        int total = 0, correctCount = 0;
        Set<String> studyDays = new HashSet<>();

        for (UserAnswer answer : answers) {
            QuestionBank question = questionMap.get(answer.getQuestionId());
            if (question == null) continue;

            // 按学科筛选
            if (subject != null && !subject.isEmpty()) {
                KnowledgePoint kp = question.getKnowledgePointId() != null
                        ? kpMap.get(question.getKnowledgePointId()) : null;
                String qSubject = kp != null ? kp.getSubject() : null;
                if (!subject.equals(qSubject)) continue;
            }

            // 按日期筛选
            LocalDateTime answeredAt = answer.getAnsweredAt();
            if (answeredAt != null) {
                if (start != null && answeredAt.isBefore(start)) continue;
                if (end != null && answeredAt.isAfter(end)) continue;
            }

            Map<String, Object> record = new HashMap<>();
            record.put("id", answer.getId());
            record.put("questionId", answer.getQuestionId());
            record.put("stem", question.getTitle());
            record.put("type", question.getType());
            record.put("userAnswer", answer.getUserAnswer());
            record.put("correctAnswer", parseAnswer(question.getAnswer()));
            record.put("isCorrect", answer.getIsCorrect());
            record.put("answeredAt", answeredAt != null ? answeredAt.toString() : null);
            if (question.getKnowledgePointId() != null) {
                KnowledgePoint kp = kpMap.get(question.getKnowledgePointId());
                if (kp != null) {
                    record.put("subject", kp.getSubject());
                    record.put("knowledgePoint", kp.getName());
                }
            }
            records.add(record);

            total++;
            if (Boolean.TRUE.equals(answer.getIsCorrect())) correctCount++;
            if (answeredAt != null) {
                studyDays.add(answeredAt.toLocalDate().toString());
            }
        }

        // 统计
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("correctCount", correctCount);
        stats.put("correctRate", total > 0 ? Math.round(correctCount * 100.0 / total) : 0);
        stats.put("studyDays", studyDays.size());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("stats", stats);
        return result;
    }

    private String parseAnswer(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            if (inner.startsWith("\"") && inner.endsWith("\"")) {
                inner = inner.substring(1, inner.length() - 1);
            }
            return inner;
        }
        return trimmed;
    }
}
