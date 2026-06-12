package com.zhiketong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiketong.entity.MistakeBook;
import com.zhiketong.mapper.MistakeBookMapper;
import com.zhiketong.service.MistakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MistakeServiceImpl implements MistakeService {

    @Autowired  // 自动注入 Mapper，用来操作数据库
    private MistakeBookMapper mistakeBookMapper;

    @Override
    @Transactional  // 数据库事务，保证数据一致性
    public void addMistake(Long userId, Long questionId, Long userAnswerId) {
        // 1. 检查这道题是否已经在错题本中（防止重复添加）
        LambdaQueryWrapper<MistakeBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeBook::getUserId, userId)
               .eq(MistakeBook::getQuestionId, questionId);
        long count = mistakeBookMapper.selectCount(wrapper);
        if (count > 0) {
            return; // 已经存在，直接返回
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
    public List<MistakeBook> getMistakesByUser(Long userId) {
        // 构造查询条件：按用户ID过滤，按添加时间倒序
        LambdaQueryWrapper<MistakeBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeBook::getUserId, userId)
               .orderByDesc(MistakeBook::getAddedAt);
        return mistakeBookMapper.selectList(wrapper);
    }

    @Override
    public void removeMistake(Long userId, Long questionId) {
        // 删除匹配用户ID和题目ID的记录
        LambdaQueryWrapper<MistakeBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeBook::getUserId, userId)
               .eq(MistakeBook::getQuestionId, questionId);
        mistakeBookMapper.delete(wrapper);
    }
}