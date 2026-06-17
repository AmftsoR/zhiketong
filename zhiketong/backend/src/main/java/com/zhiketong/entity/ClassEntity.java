package com.zhiketong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 班级实体，对应数据库表 class
 */
@Data
@TableName("class")
public class ClassEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long teacherId;
    private LocalDateTime createTime;
}
