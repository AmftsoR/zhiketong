package com.zhiketong.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question_bank")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long knowledgePointId;
    private String type;
    private String difficulty;
    private String title;
    private String options;
    private String answer;
    private String explanation;
    private LocalDateTime createdAt;
}
