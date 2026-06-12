package com.zhiketong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_bank")
public class QuestionBank {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String title;
    private String options;   // 数据库是 JSON，Java中用String接收，然后手动转
    private String answer;
    private String explanation;
    private String difficulty;
    private Long knowledgePointId;
    private LocalDateTime createdAt;
}