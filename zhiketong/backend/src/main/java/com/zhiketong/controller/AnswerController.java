package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.entity.MistakeBook;
import com.zhiketong.entity.QuestionBank;
import com.zhiketong.entity.UserAnswer;
import com.zhiketong.mapper.MistakeBookMapper;
import com.zhiketong.mapper.QuestionBankMapper;
import com.zhiketong.mapper.UserAnswerMapper;
import com.zhiketong.service.MistakeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/answer")
public class AnswerController {

    @Autowired
    private QuestionBankMapper questionBankMapper;
    @Autowired
    private UserAnswerMapper userAnswerMapper;
    @Autowired
    private MistakeBookMapper mistakeBookMapper;
    @Autowired
    private MistakeService mistakeService;

    /**
     * 学生提交答案
     * POST /api/answer/submit
     * 请求体示例: {"questionId": 1, "userAnswer": "A"}
     */
    @PostMapping("/submit")
    public R<Map<String, Object>> submitAnswer(@RequestBody Map<String, Object> payload,
                                               HttpServletRequest request) {
        // 从登录拦截器注入的 attribute 获取当前用户 ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            userId = 1L; // fallback：未登录时使用默认用户
        }
        Long questionId = Long.valueOf(payload.get("questionId").toString());
        String userAnswer = payload.get("userAnswer").toString();

        // 查询题目
        QuestionBank question = questionBankMapper.selectById(questionId);
        if (question == null) {
            return R.fail("题目不存在");
        }

        // 解析正确答案（兼容 JSON 数组格式 ["B"]）
        String correctAnswer = parseAnswer(question.getAnswer());

        // 判题
        boolean isCorrect = userAnswer.equals(correctAnswer);

        // 保存答题记录
        UserAnswer answerRecord = new UserAnswer();
        answerRecord.setUserId(userId);
        answerRecord.setQuestionId(questionId);
        answerRecord.setUserAnswer(userAnswer);
        answerRecord.setIsCorrect(isCorrect);
        answerRecord.setAnsweredAt(LocalDateTime.now());
        userAnswerMapper.insert(answerRecord);

        // 如果答错，加入错题本
        if (!isCorrect) {
            mistakeService.addMistake(userId, questionId, answerRecord.getId());
            return R.ok(Map.of("isCorrect", false, "message", "回答错误，已加入错题本"));
        }

        // 答对了：自动标记同知识点错题为"已掌握"
        markMistakesAsMastered(userId, question);

        return R.ok(Map.of("isCorrect", true, "message", "回答正确"));
    }

    /**
     * 答对题目时，将错题本中同一知识点的错题标记为"已掌握"
     */
    private void markMistakesAsMastered(Long userId, QuestionBank question) {
        Long knowledgePointId = question.getKnowledgePointId();
        if (knowledgePointId == null) return;

        // 查找用户的所有错题记录
        LambdaQueryWrapper<MistakeBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MistakeBook::getUserId, userId);
        List<MistakeBook> mistakes = mistakeBookMapper.selectList(queryWrapper);
        if (mistakes == null || mistakes.isEmpty()) return;

        List<Long> masteredIds = mistakes.stream()
                .filter(m -> m.getQuestionId() != null)
                .filter(m -> {
                    QuestionBank q = questionBankMapper.selectById(m.getQuestionId());
                    return q != null && knowledgePointId.equals(q.getKnowledgePointId());
                })
                .map(MistakeBook::getId)
                .collect(Collectors.toList());

        if (!masteredIds.isEmpty()) {
            LambdaUpdateWrapper<MistakeBook> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(MistakeBook::getId, masteredIds)
                         .set(MistakeBook::getMastered, 1);
            mistakeBookMapper.update(null, updateWrapper);
        }
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
