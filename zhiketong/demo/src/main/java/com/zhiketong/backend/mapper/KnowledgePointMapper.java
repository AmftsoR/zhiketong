package com.zhiketong.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiketong.backend.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgePointMapper extends BaseMapper<KnowledgePoint> {

    @Select("SELECT * FROM knowledge_point WHERE parent_id = #{parentId} ORDER BY id")
    List<KnowledgePoint> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM knowledge_point WHERE name = #{name} LIMIT 1")
    KnowledgePoint selectByName(@Param("name") String name);

    @Select("SELECT * FROM knowledge_point ORDER BY subject, parent_id, id")
    List<KnowledgePoint> selectAllActive();
}
