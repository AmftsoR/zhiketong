package com.zhiketong.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiketong.backend.dto.KnowledgePointStatVO;
import com.zhiketong.backend.entity.UserAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    List<Long> selectWeakKnowledgePointIds(@Param("userId") Long userId, @Param("limit") Integer limit);

    List<KnowledgePointStatVO> selectKnowledgePointStats(
            @Param("studentIds") List<Long> studentIds,
            @Param("knowledgePointIds") List<Long> knowledgePointIds,
            @Param("className") String className
    );
}
