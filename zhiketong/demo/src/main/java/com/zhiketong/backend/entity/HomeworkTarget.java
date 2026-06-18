package com.zhiketong.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("homework_target")
public class HomeworkTarget {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long homeworkId;
    private String targetType;
    private Long targetId;
    private String className;
    private LocalDateTime createdAt;
}
