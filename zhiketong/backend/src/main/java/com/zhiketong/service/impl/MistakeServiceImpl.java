package com.zhiketong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiketong.entity.KnowledgePoint;
import com.zhiketong.entity.MistakeBook;
import com.zhiketong.entity.QuestionBank;
import com.zhiketong.entity.UserAnswer;
import com.zhiketong.mapper.KnowledgePointMapper;
import com.zhiketong.mapper.MistakeBookMapper;
import com.zhiketong.mapper.QuestionBankMapper;
import com.zhiketong.mapper.UserAnswerMapper;
import com.zhiketong.service.MistakeService;
import com.zhiketong.vo.MistakeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MistakeServiceImpl implements MistakeService {

    @Autowired
    private MistakeBookMapper mistakeBookMapper;
    @Autowired
    private QuestionBankMapper questionBankMapper;
    @Autowired
    private UserAnswerMapper userAnswerMapper;
    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Override
    @Transactional
    public void addMistake(Long userId, Long questionId, Long userAnswerId) {
        // 1. 检查这道题是否已经在错题本中（防止重复添加）
        LambdaQueryWrapper<MistakeBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeBook::getUserId, userId)
               .eq(MistakeBook::getQuestionId, questionId);
        long count = mistakeBookMapper.selectCount(wrapper);
        if (count > 0) {
            return;
        }

        // 2. 创建新的错题记录
        MistakeBook mistake = new MistakeBook();
        mistake.setUserId(userId);
        mistake.setQuestionId(questionId);
        mistake.setUserAnswerId(userAnswerId);
        mistake.setAddedAt(LocalDateTime.now());

        // 3. 插入数据库
        mistakeBookMapper.insert(mistake);
    }

    @Override
    public List<MistakeVO> getMistakesByUser(Long userId) {
        // 1. 查询该用户的错题记录（按时间倒序）
        LambdaQueryWrapper<MistakeBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeBook::getUserId, userId)
               .orderByDesc(MistakeBook::getAddedAt);
        List<MistakeBook> mistakes = mistakeBookMapper.selectList(wrapper);

        if (mistakes.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 批量查询关联的题目
        List<Long> questionIds = mistakes.stream()
                .map(MistakeBook::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        List<QuestionBank> questions = questionBankMapper.selectBatchIds(questionIds);
        Map<Long, QuestionBank> questionMap = questions.stream()
                .collect(Collectors.toMap(QuestionBank::getId, q -> q));

        // 3. 批量查询关联的答题记录
        List<Long> answerIds = mistakes.stream()
                .map(MistakeBook::getUserAnswerId)
                .distinct()
                .collect(Collectors.toList());
        List<UserAnswer> answers = userAnswerMapper.selectBatchIds(answerIds);
        Map<Long, UserAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(UserAnswer::getId, a -> a));

        // 4. 批量查询知识点（可选）
        List<Long> kpIds = questions.stream()
                .map(QuestionBank::getKnowledgePointId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, KnowledgePoint> kpMap = Map.of();
        if (!kpIds.isEmpty()) {
            List<KnowledgePoint> kps = knowledgePointMapper.selectBatchIds(kpIds);
            kpMap = kps.stream().collect(Collectors.toMap(KnowledgePoint::getId, k -> k));
        }

        // 统计每道题的累计错题次数
        Map<Long, Long> wrongCountMap = mistakes.stream()
                .collect(Collectors.groupingBy(MistakeBook::getQuestionId, Collectors.counting()));

        // 5. 组装 VO
        List<MistakeVO> result = new ArrayList<>();
        for (MistakeBook mistake : mistakes) {
            QuestionBank question = questionMap.get(mistake.getQuestionId());
            UserAnswer answer = answerMap.get(mistake.getUserAnswerId());

            MistakeVO vo = new MistakeVO();
            vo.setId(mistake.getId());
            vo.setQuestionId(mistake.getQuestionId());
            vo.setAddedAt(mistake.getAddedAt());
            vo.setWrongCount(wrongCountMap.getOrDefault(mistake.getQuestionId(), 1L).intValue());

            if (question != null) {
                vo.setType(question.getType());
                vo.setDifficulty(question.getDifficulty());
                vo.setStem(question.getTitle());
                vo.setOptions(question.getOptions());
                vo.setCorrectAnswer(parseAnswer(question.getAnswer()));
                if (question.getKnowledgePointId() != null && kpMap.containsKey(question.getKnowledgePointId())) {
                    KnowledgePoint kp = kpMap.get(question.getKnowledgePointId());
                    vo.setKnowledgePoint(kp.getName());
                    vo.setSubject(kp.getSubject());
                }
            }

            if (answer != null) {
                vo.setMyAnswer(answer.getUserAnswer());
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public void removeMistake(Long userId, Long questionId) {
        LambdaQueryWrapper<MistakeBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeBook::getUserId, userId)
               .eq(MistakeBook::getQuestionId, questionId);
        mistakeBookMapper.delete(wrapper);
    }

    /**
     * 解析正确答案：兼容 JSON 数组格式（如 ["B"] → "B"）
     */
    private String parseAnswer(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            // 去掉引号
            if (inner.startsWith("\"") && inner.endsWith("\"")) {
                inner = inner.substring(1, inner.length() - 1);
            }
            return inner;
        }
        return trimmed;
    }
}
