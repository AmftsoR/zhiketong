package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.entity.QuestionBank;
import com.zhiketong.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 题库控制器 — 提供随机出题等接口
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 获取一道随机题目（不含正确答案和解析）
     * GET /api/questions/random
     */
    @GetMapping("/random")
    public R<QuestionBank> randomQuestion() {
        QuestionBank question = questionService.getRandomQuestion();
        if (question == null) {
            return R.fail("题库暂无数据");
        }
        // 前端做题时不应返回正确答案，置为空
        question.setAnswer(null);
        question.setExplanation(null);
        return R.ok(question);
    }
}
