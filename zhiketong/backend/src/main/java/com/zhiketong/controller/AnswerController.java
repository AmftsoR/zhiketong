package com.zhiketong.controller;

import com.zhiketong.entity.QuestionBank;
import com.zhiketong.entity.UserAnswer;
import com.zhiketong.mapper.QuestionBankMapper;
import com.zhiketong.mapper.UserAnswerMapper;
import com.zhiketong.service.MistakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/answer")
public class AnswerController {

    @Autowired
    private QuestionBankMapper questionBankMapper;
    @Autowired
    private UserAnswerMapper userAnswerMapper;
    @Autowired
    private MistakeService mistakeService;

    /**
     * 学生提交答案
     * POST /api/answer/submit
     * 请求体示例: {"questionId": 1, "userAnswer": "A"}
     */
    @PostMapping("/submit")
    public String submitAnswer(@RequestBody Map<String, Object> payload) {
        // 1. 从请求中取出数据
        Long userId = 1L;  // 暂时写死，后续从token获取
        Long questionId = Long.valueOf(payload.get("questionId").toString());
        String userAnswer = payload.get("userAnswer").toString();

        // 2. 查询题目
        QuestionBank question = questionBankMapper.selectById(questionId);
        if (question == null) {
            return "题目不存在";
        }
        String correctAnswer = question.getAnswer();

        // 3. 判题（简单字符串比较，实际可能更复杂）
        boolean isCorrect = userAnswer.equals(correctAnswer);

        // 4. 保存答题记录
        UserAnswer answerRecord = new UserAnswer();
        answerRecord.setUserId(userId);
        answerRecord.setQuestionId(questionId);
        answerRecord.setUserAnswer(userAnswer);
        answerRecord.setIsCorrect(isCorrect);
        answerRecord.setAnsweredAt(LocalDateTime.now());
        userAnswerMapper.insert(answerRecord);

        // 5. 如果答错，加入错题本
        if (!isCorrect) {
            mistakeService.addMistake(userId, questionId, answerRecord.getId());
            return "回答错误，已加入错题本";
        } else {
            return "回答正确";
        }
    }
}