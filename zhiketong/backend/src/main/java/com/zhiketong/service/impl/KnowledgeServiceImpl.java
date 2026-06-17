package com.zhiketong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiketong.dto.KnowledgeMasteryDto;
import com.zhiketong.entity.KnowledgePoint;
import com.zhiketong.entity.QuestionBank;
import com.zhiketong.entity.UserAnswer;
import com.zhiketong.mapper.KnowledgePointMapper;
import com.zhiketong.mapper.QuestionBankMapper;
import com.zhiketong.mapper.UserAnswerMapper;
import com.zhiketong.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 薄弱点图谱服务 — 基于 user_answer 计算掌握度
 */
@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Autowired
    private UserAnswerMapper userAnswerMapper;

    @Autowired
    private QuestionBankMapper questionBankMapper;

    @Override
    public List<KnowledgeMasteryDto> getStudentMastery(Long studentId) {
        // 1. 查询该学生的所有答题记录
        LambdaQueryWrapper<UserAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswer::getUserId, studentId);
        List<UserAnswer> answers = userAnswerMapper.selectList(wrapper);

        // 2. 如果没有答题记录，返回所有知识点掌握度为 0（红色）
        if (answers.isEmpty()) {
            return getEmptyMasteryList();
        }

        // 3. 获取答题涉及的题目 ID
        List<Long> questionIds = answers.stream()
                .map(UserAnswer::getQuestionId)
                .distinct()
                .collect(Collectors.toList());

        // 4. 查询题目信息，建立 题目ID → 知识点ID 的映射
        List<QuestionBank> questions = questionBankMapper.selectBatchIds(questionIds);
        Map<Long, Long> questionToKnowledge = questions.stream()
                .collect(Collectors.toMap(QuestionBank::getId, QuestionBank::getKnowledgePointId));

        // 5. 按知识点分组统计：总答题数 和 正确数
        Map<Long, Integer> totalCount = new HashMap<>();
        Map<Long, Integer> correctCount = new HashMap<>();

        for (UserAnswer ua : answers) {
            Long kpId = questionToKnowledge.get(ua.getQuestionId());
            if (kpId == null) continue;
            totalCount.put(kpId, totalCount.getOrDefault(kpId, 0) + 1);
            if (Boolean.TRUE.equals(ua.getIsCorrect())) {
                correctCount.put(kpId, correctCount.getOrDefault(kpId, 0) + 1);
            }
        }

        // 6. 获取所有知识点，计算每个知识点的掌握度
        List<KnowledgePoint> allPoints = knowledgePointMapper.selectList(null);
        List<KnowledgeMasteryDto> result = new ArrayList<>();

        for (KnowledgePoint kp : allPoints) {
            KnowledgeMasteryDto dto = new KnowledgeMasteryDto();
            dto.setKnowledgeId(kp.getId());
            dto.setKnowledgeName(kp.getName());

            int total = totalCount.getOrDefault(kp.getId(), 0);
            int correct = correctCount.getOrDefault(kp.getId(), 0);
            double rate = total == 0 ? 0 : (double) correct / total;
            dto.setCorrectRate(rate);

            // 红黄绿分级
            if (rate >= 0.8) {
                dto.setColor("green");
            } else if (rate >= 0.5) {
                dto.setColor("yellow");
            } else {
                dto.setColor("red");
            }
            result.add(dto);
        }
        return result;
    }

    private List<KnowledgeMasteryDto> getEmptyMasteryList() {
        List<KnowledgePoint> allPoints = knowledgePointMapper.selectList(null);
        return allPoints.stream().map(kp -> {
            KnowledgeMasteryDto dto = new KnowledgeMasteryDto();
            dto.setKnowledgeId(kp.getId());
            dto.setKnowledgeName(kp.getName());
            dto.setCorrectRate(0.0);
            dto.setColor("red");
            return dto;
        }).collect(Collectors.toList());
    }
}
