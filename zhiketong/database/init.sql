-- ============================================================
-- 知课堂 (ZhiKeTong) 数据库初始化脚本
-- 数据库名: zhiketong_db
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `zhiketong_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `zhiketong_db`;

-- -----------------------------------------------------------
-- 1. 班级表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `class` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `name`        VARCHAR(50)  NOT NULL              COMMENT '班级名称',
  `teacher_id`  BIGINT       DEFAULT NULL          COMMENT '班主任ID',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- -----------------------------------------------------------
-- 2. 用户表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`   VARCHAR(50)  NOT NULL              COMMENT '用户名',
  `password`   VARCHAR(255) NOT NULL              COMMENT '密码（BCrypt加密）',
  `role`       VARCHAR(20)  NOT NULL DEFAULT 'student' COMMENT '角色：student/teacher',
  `real_name`  VARCHAR(50)  DEFAULT NULL          COMMENT '真实姓名',
  `class_id`   BIGINT       DEFAULT NULL          COMMENT '所属班级ID',
  `class_name` VARCHAR(50)  DEFAULT NULL          COMMENT '班级名称（冗余）',
  `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 知识点表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `knowledge_point` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
  `name`       VARCHAR(100) NOT NULL              COMMENT '知识点名称',
  `parent_id`  BIGINT       DEFAULT NULL          COMMENT '父知识点ID（用于层级）',
  `subject`    VARCHAR(20)  NOT NULL              COMMENT '学科：math/physics/english',
  `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点表';

-- -----------------------------------------------------------
-- 3. 题库表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `question_bank` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `type`               VARCHAR(20)  NOT NULL              COMMENT '题型：single_choice/multiple_choice/true_false/subjective',
  `title`              TEXT         NOT NULL              COMMENT '题干',
  `options`            JSON         DEFAULT NULL          COMMENT '选项（JSON数组，非选择题时可为空）',
  `answer`             JSON         NOT NULL              COMMENT '正确答案（支持多选题，JSON数组格式）',
  `explanation`        TEXT         DEFAULT NULL          COMMENT '解析',
  `difficulty`         VARCHAR(10)  DEFAULT 'medium'      COMMENT '难度：easy/medium/hard',
  `knowledge_point_id` BIGINT       DEFAULT NULL          COMMENT '关联知识点ID',
  `created_at`         DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_kp` (`knowledge_point_id`),
  CONSTRAINT `fk_question_kp` FOREIGN KEY (`knowledge_point_id`)
    REFERENCES `knowledge_point` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库表';

-- -----------------------------------------------------------
-- 4. 用户答题记录表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_answer` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL              COMMENT '用户ID',
  `question_id` BIGINT       NOT NULL              COMMENT '题目ID',
  `user_answer` VARCHAR(10)  DEFAULT NULL          COMMENT '用户提交的答案',
  `is_correct`  TINYINT(1)   NOT NULL              COMMENT '是否正确',
  `answered_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_question` (`question_id`),
  CONSTRAINT `fk_answer_user` FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`)
    REFERENCES `question_bank` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户答题记录表';

-- -----------------------------------------------------------
-- 5. 错题本表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mistake_book` (
  `id`             BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT   NOT NULL,
  `question_id`    BIGINT   NOT NULL,
  `user_answer_id` BIGINT   NOT NULL              COMMENT '关联的那次答题记录',
  `added_at`       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入错题本时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_question` (`question_id`),
  KEY `fk_mistake_answer` (`user_answer_id`),
  CONSTRAINT `fk_mistake_user` FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_mistake_question` FOREIGN KEY (`question_id`)
    REFERENCES `question_bank` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_mistake_answer` FOREIGN KEY (`user_answer_id`)
    REFERENCES `user_answer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错题本表';

-- -----------------------------------------------------------
-- 测试数据（可选）
-- -----------------------------------------------------------
-- INSERT INTO `user` (`username`, `password`, `role`, `real_name`, `class_name`)
-- VALUES ('student01', '123456', 'student', '张三', '高一(3)班');
