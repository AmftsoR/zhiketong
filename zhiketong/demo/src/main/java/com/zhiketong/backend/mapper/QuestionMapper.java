package com.zhiketong.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiketong.backend.entity.Question;
import com.zhiketong.backend.dto.QuestionQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    Page<Question> selectPageByQuery(Page<Question> page, @Param("query") QuestionQuery query);

    List<Question> selectByKnowledgePointsAndDifficulty(
            @Param("kpIds") List<Long> kpIds,
            @Param("difficulty") String difficulty,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("limit") Integer limit
    );

    List<Question> selectByIdsNotIn(
            @Param("kpId") Long kpId,
            @Param("ids") List<Long> ids,
            @Param("limit") Integer limit
    );
}
