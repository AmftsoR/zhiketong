package com.zhiketong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiketong.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

    /**
     * 随机获取一条题目
     */
    @Select("SELECT * FROM question_bank ORDER BY RAND() LIMIT 1")
    QuestionBank selectRandomOne();
}
