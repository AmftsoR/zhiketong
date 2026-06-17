package com.zhiketong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiketong.dto.WrongRecordDetailDto;
import com.zhiketong.entity.MistakeBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MistakeBookMapper extends BaseMapper<MistakeBook> {

    /**
     * 连表查询错题详情（mistake_book + question_bank + user_answer + knowledge_point）
     */
    @Select("SELECT mb.id as wrongId, mb.question_id as questionId, qb.title as questionContent, " +
            "qb.answer as correctAnswer, ua.user_answer as wrongAnswer, kp.name as knowledgeName, " +
            "ua.answered_at as wrongTime " +
            "FROM mistake_book mb " +
            "LEFT JOIN question_bank qb ON mb.question_id = qb.id " +
            "LEFT JOIN user_answer ua ON mb.user_answer_id = ua.id " +
            "LEFT JOIN knowledge_point kp ON qb.knowledge_point_id = kp.id " +
            "WHERE mb.user_id = #{userId} " +
            "ORDER BY mb.added_at DESC")
    List<WrongRecordDetailDto> selectWrongDetailByUserId(@Param("userId") Long userId);
}