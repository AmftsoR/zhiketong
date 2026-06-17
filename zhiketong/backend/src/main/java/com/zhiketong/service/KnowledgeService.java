package com.zhiketong.service;

import com.zhiketong.dto.KnowledgeMasteryDto;
import java.util.List;

/**
 * 知识点掌握度服务接口
 */
public interface KnowledgeService {

    /**
     * 获取学生的知识点掌握度列表（红黄绿分级）
     */
    List<KnowledgeMasteryDto> getStudentMastery(Long studentId);
}
