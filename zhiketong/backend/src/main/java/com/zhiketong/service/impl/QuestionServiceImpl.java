package com.zhiketong.service.impl;

import com.zhiketong.entity.QuestionBank;
import com.zhiketong.mapper.QuestionBankMapper;
import com.zhiketong.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionBankMapper questionBankMapper;

    @Override
    public QuestionBank getRandomQuestion() {
        return questionBankMapper.selectRandomOne();
    }
}
