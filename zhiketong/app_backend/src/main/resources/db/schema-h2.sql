-- 用户表
CREATE TABLE IF NOT EXISTS `app_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `name` VARCHAR(50),
    `role` VARCHAR(20) NOT NULL DEFAULT 'student',
    `class_id` BIGINT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
    );
-- 班级表
CREATE TABLE IF NOT EXISTS `class` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `teacher_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
    );

-- 题目表（简化版）
CREATE TABLE IF NOT EXISTS `question` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `content` TEXT NOT NULL,
    `answer` VARCHAR(10) NOT NULL,
    `knowledge_point_id` BIGINT,
    `difficulty` VARCHAR(20)
    );

-- 知识点表
CREATE TABLE IF NOT EXISTS `knowledge_point` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `subject` VARCHAR(50),
    `parent_id` BIGINT
    );

-- 错题记录表
CREATE TABLE IF NOT EXISTS `wrong_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `wrong_answer` VARCHAR(10),
    `wrong_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `is_uploaded` TINYINT DEFAULT 0,
    `image_url` VARCHAR(500)
    );

-- 练习记录表
CREATE TABLE IF NOT EXISTS `practice_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
     `user_id` BIGINT NOT NULL,
     `question_id` BIGINT NOT NULL,
     `answer` VARCHAR(10),
    `is_correct` TINYINT,
    `practice_time` DATETIME DEFAULT CURRENT_TIMESTAMP
    );

-- 学生知识点掌握表
CREATE TABLE IF NOT EXISTS `user_knowledge` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `knowledge_point_id` BIGINT NOT NULL,
    `correct_rate` DECIMAL(5,2),
    `status` VARCHAR(10),
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_kp` (`user_id`, `knowledge_point_id`)
    );