-- =============================================
-- 智课通 数据库建表脚本 (MySQL)
-- 包含 backend 全部表 + app_backend 扩展表
-- =============================================

-- =============================================
-- 一、backend 原有表（原样保留）
-- =============================================

-- 1. 用户表（backend 原设计 + app_backend 的 class_id 扩展）
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `real_name` VARCHAR(50) COMMENT '真实姓名（backend字段）',
    `role` VARCHAR(20) NOT NULL DEFAULT 'student' COMMENT 'student 或 teacher',
    `class_name` VARCHAR(100) COMMENT '班级名称（backend字段，冗余存储）',
    `class_id` BIGINT DEFAULT NULL COMMENT '班级ID（app_backend字段，外键关联class表）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 知识点表
CREATE TABLE IF NOT EXISTS `knowledge_point` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `subject` VARCHAR(50) COMMENT '学科',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父知识点ID，用于树形结构'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 题库表（backend 完整设计）
CREATE TABLE IF NOT EXISTS `question_bank` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `type` VARCHAR(20) COMMENT '题型：choice/blank/judge/short',
    `title` TEXT NOT NULL COMMENT '题干',
    `options` TEXT COMMENT '选项（JSON格式，选择题使用）',
    `answer` VARCHAR(100) NOT NULL COMMENT '正确答案',
    `explanation` TEXT COMMENT '答案解析',
    `difficulty` VARCHAR(20) COMMENT '难度：easy/medium/hard',
    `knowledge_point_id` BIGINT COMMENT '关联知识点ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 答题记录表（backend 设计，记录每次答题的原始数据）
CREATE TABLE IF NOT EXISTS `user_answer` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `user_answer` VARCHAR(500) COMMENT '学生提交的答案（可能是JSON）',
    `is_correct` TINYINT(1) COMMENT '是否正确：0错误/1正确',
    `answered_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 错题本表（backend 设计，关联答题记录）
CREATE TABLE IF NOT EXISTS `mistake_book` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `user_answer_id` BIGINT COMMENT '关联的答题记录ID',
    `added_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 二、app_backend 新增表（扩展功能）
-- =============================================

-- 6. 班级表
CREATE TABLE IF NOT EXISTS `class` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `teacher_id` BIGINT NOT NULL COMMENT '班主任教师ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 练习记录表（app_backend 的练习统计，比 user_answer 更适合做数据分析）
CREATE TABLE IF NOT EXISTS `practice_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `answer` VARCHAR(100) COMMENT '学生提交的答案',
    `is_correct` TINYINT DEFAULT 0 COMMENT '是否正确：0错误/1正确',
    `practice_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 错题记录表（app_backend 设计，支持拍照上传，比 mistake_book 功能更完整）
CREATE TABLE IF NOT EXISTS `wrong_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `wrong_answer` VARCHAR(100) COMMENT '学生的错误答案',
    `wrong_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `is_uploaded` TINYINT DEFAULT 0 COMMENT '0:自动收录 1:拍照上传',
    `image_url` VARCHAR(500) COMMENT '拍照上传的图片地址'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 学生知识点掌握度表（薄弱点图谱功能）
CREATE TABLE IF NOT EXISTS `user_knowledge` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `knowledge_point_id` BIGINT NOT NULL,
    `correct_rate` DECIMAL(5,2) COMMENT '正确率 0.00-100.00',
    `status` VARCHAR(10) COMMENT '掌握状态：weak/normal/strong',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_kp` (`user_id`, `knowledge_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
