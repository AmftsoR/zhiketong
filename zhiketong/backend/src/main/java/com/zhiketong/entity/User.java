package com.zhiketong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体，对应数据库表 user
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String role;            // student 或 teacher
    @TableField("real_name")
    private String realName;        // 真实姓名
    private Long classId;           // 所属班级ID（外键关联 class.id）
    @TableField("class_name")
    private String className;       // 班级名称（冗余字段，方便展示）
    @TableField("created_at")
    private LocalDateTime createdAt;
}