package com.zhiketong.service;

import com.zhiketong.entity.QuestionBank;

/**
 * 题库服务接口
 */
public interface QuestionService {

    /**
     * 随机获取一道题目
     * @return 随机题目
     */
    QuestionBank getRandomQuestion();
}
