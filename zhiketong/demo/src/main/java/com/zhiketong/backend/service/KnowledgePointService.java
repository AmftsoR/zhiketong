package com.zhiketong.backend.service;

import com.zhiketong.backend.dto.KnowledgePointCreateDTO;
import com.zhiketong.backend.dto.KnowledgePointGenerateDTO;
import com.zhiketong.backend.dto.KnowledgePointMoveDTO;
import com.zhiketong.backend.dto.KnowledgePointTreeVO;
import com.zhiketong.backend.dto.KnowledgePointUpdateDTO;
import com.zhiketong.backend.entity.KnowledgePoint;

import java.util.List;

public interface KnowledgePointService {

    List<KnowledgePointTreeVO> getTree();

    KnowledgePoint create(KnowledgePointCreateDTO dto);

    KnowledgePoint update(KnowledgePointUpdateDTO dto);

    void delete(Long id);

    void move(Long id, KnowledgePointMoveDTO dto);

    List<KnowledgePoint> getByIds(List<Long> ids);

    /** AI 生成知识点树 */
    List<KnowledgePointTreeVO> generate(KnowledgePointGenerateDTO dto);
}
