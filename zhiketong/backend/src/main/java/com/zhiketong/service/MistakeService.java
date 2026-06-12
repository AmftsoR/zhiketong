package com.zhiketong.service;

import com.zhiketong.entity.MistakeBook;
import java.util.List;

/**
 * 错题本业务逻辑接口
 */
public interface MistakeService {
    /**
     * 添加错题记录
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param userAnswerId 答题记录ID
     */
    void addMistake(Long userId, Long questionId, Long userAnswerId);

    /**
     * 获取某个用户的所有错题
     * @param userId 用户ID
     * @return 错题列表
     */
    List<MistakeBook> getMistakesByUser(Long userId);

    /**
     * 删除某道错题
     * @param userId 用户ID
     * @param questionId 题目ID
     */
    void removeMistake(Long userId, Long questionId);
}