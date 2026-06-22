package com.zhiketong.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiketong.backend.common.BusinessException;
import com.zhiketong.backend.dto.ExercisePushRequestDTO;
import com.zhiketong.backend.dto.ExercisePushVO;
import com.zhiketong.backend.dto.ExerciseSubmitRequestDTO;
import com.zhiketong.backend.dto.ExerciseSubmitVO;
import com.zhiketong.backend.entity.MistakeBook;
import com.zhiketong.backend.entity.Question;
import com.zhiketong.backend.entity.UserAnswer;
import com.zhiketong.backend.mapper.MistakeBookMapper;
import com.zhiketong.backend.mapper.QuestionMapper;
import com.zhiketong.backend.mapper.UserAnswerMapper;
import com.zhiketong.backend.service.ExerciseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExerciseServiceImpl implements ExerciseService {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private UserAnswerMapper userAnswerMapper;

    @Resource
    private MistakeBookMapper mistakeBookMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public ExercisePushVO push(ExercisePushRequestDTO dto) {
        if (dto.getUserId() == null) {
            throw new BusinessException("学生ID不能为空");
        }

        int totalCount = dto.getTotalCount() == null || dto.getTotalCount() <= 0 ? 10 : dto.getTotalCount();
        List<Long> kpIds = resolveKnowledgePointIds(dto);
        Map<String, Integer> distribution = resolveDistribution(dto, totalCount);

        List<Question> questions = new ArrayList<>();
        List<Long> excludeIds = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            List<Question> picked = questionMapper.selectByKnowledgePointsAndDifficulty(
                    kpIds,
                    entry.getKey(),
                    excludeIds,
                    entry.getValue()
            );
            questions.addAll(picked);
            picked.stream().map(Question::getId).forEach(excludeIds::add);
        }

        int missing = totalCount - questions.size();
        if (missing > 0) {
            List<Question> fallback = questionMapper.selectByKnowledgePointsAndDifficulty(kpIds, null, excludeIds, missing);
            questions.addAll(fallback);
        }

        ExercisePushVO vo = new ExercisePushVO();
        vo.setUserId(dto.getUserId());
        vo.setWeakKnowledgePointIds(kpIds);
        vo.setDifficultyDistribution(distribution);
        vo.setQuestions(questions.stream()
                .sorted(Comparator.comparing(Question::getDifficulty, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional
    public ExerciseSubmitVO submit(ExerciseSubmitRequestDTO dto) {
        if (dto.getUserId() == null) {
            throw new BusinessException("学生ID不能为空");
        }
        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            throw new BusinessException("提交答案不能为空");
        }

        List<Long> questionIds = dto.getAnswers().stream()
                .map(ExerciseSubmitRequestDTO.AnswerItem::getQuestionId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Question> questionMap = questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        ExerciseSubmitVO vo = new ExerciseSubmitVO();
        vo.setTotalCount(dto.getAnswers().size());

        for (ExerciseSubmitRequestDTO.AnswerItem item : dto.getAnswers()) {
            Question question = questionMap.get(item.getQuestionId());
            if (question == null) {
                throw new BusinessException("题目不存在: " + item.getQuestionId());
            }

            boolean correct = sameAnswer(item.getUserAnswer(), question.getAnswer());
            UserAnswer record = new UserAnswer();
            record.setUserId(dto.getUserId());
            record.setQuestionId(question.getId());
            record.setUserAnswer(item.getUserAnswer());
            record.setIsCorrect(correct);
            userAnswerMapper.insert(record);

            boolean addedToMistakeBook = false;
            if (!correct) {
                addedToMistakeBook = addMistakeIfAbsent(dto.getUserId(), question.getId(), record.getId());
            }

            ExerciseSubmitVO.AnswerResult detail = new ExerciseSubmitVO.AnswerResult();
            detail.setQuestionId(question.getId());
            detail.setUserAnswer(item.getUserAnswer());
            detail.setCorrectAnswer(question.getAnswer());
            detail.setCorrect(correct);
            detail.setExplanation(question.getExplanation());
            detail.setAddedToMistakeBook(addedToMistakeBook);
            vo.getDetails().add(detail);

            if (correct) {
                vo.setCorrectCount(vo.getCorrectCount() + 1);
            }
        }

        vo.setWrongCount(vo.getTotalCount() - vo.getCorrectCount());
        vo.setScore(vo.getTotalCount() == 0 ? 0 : Math.round(vo.getCorrectCount() * 10000.0 / vo.getTotalCount()) / 100.0);
        return vo;
    }

    private List<Long> resolveKnowledgePointIds(ExercisePushRequestDTO dto) {
        if (dto.getKnowledgePointIds() != null && !dto.getKnowledgePointIds().isEmpty()) {
            return dto.getKnowledgePointIds();
        }
        return userAnswerMapper.selectWeakKnowledgePointIds(dto.getUserId(), 5);
    }

    private Map<String, Integer> resolveDistribution(ExercisePushRequestDTO dto, int totalCount) {
        int easy = dto.getEasyCount() == null ? Math.max(1, (int) Math.round(totalCount * 0.5)) : dto.getEasyCount();
        int medium = dto.getMediumCount() == null ? Math.max(0, (int) Math.round(totalCount * 0.3)) : dto.getMediumCount();
        int hard = dto.getHardCount() == null ? totalCount - easy - medium : dto.getHardCount();
        if (hard < 0) {
            hard = 0;
        }

        int sum = easy + medium + hard;
        if (sum < totalCount) {
            medium += totalCount - sum;
        } else if (sum > totalCount) {
            int overflow = sum - totalCount;
            int reduceHard = Math.min(hard, overflow);
            hard -= reduceHard;
            overflow -= reduceHard;
            int reduceMedium = Math.min(medium, overflow);
            medium -= reduceMedium;
            overflow -= reduceMedium;
            easy = Math.max(0, easy - overflow);
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("easy", easy);
        result.put("medium", medium);
        result.put("hard", hard);
        return result;
    }

    private boolean addMistakeIfAbsent(Long userId, Long questionId, Long userAnswerId) {
        Long count = mistakeBookMapper.selectCount(new LambdaQueryWrapper<MistakeBook>()
                .eq(MistakeBook::getUserId, userId)
                .eq(MistakeBook::getQuestionId, questionId));
        if (count != null && count > 0) {
            return false;
        }
        MistakeBook mistake = new MistakeBook();
        mistake.setUserId(userId);
        mistake.setQuestionId(questionId);
        mistake.setUserAnswerId(userAnswerId);
        mistakeBookMapper.insert(mistake);
        return true;
    }

    private boolean sameAnswer(String userAnswer, String correctAnswer) {
        return canonicalAnswer(userAnswer).equals(canonicalAnswer(correctAnswer));
    }

    private String canonicalAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        String trimmed = answer.trim();
        try {
            JsonNode node = objectMapper.readTree(trimmed);
            if (node.isArray()) {
                Set<String> values = new LinkedHashSet<>();
                node.forEach(item -> values.add(item.asText().trim().toUpperCase()));
                return values.stream().sorted().collect(Collectors.joining("|"));
            }
            if (node.isTextual() || node.isNumber() || node.isBoolean()) {
                return node.asText().trim().toUpperCase();
            }
        } catch (Exception ignored) {
            // Plain text answers are handled below.
        }
        return trimmed.replaceAll("\\s+", "").toUpperCase();
    }
}
