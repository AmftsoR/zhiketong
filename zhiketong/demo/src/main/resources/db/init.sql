-- ============================================================
-- 智课通 ZhiKeTong 数据库初始化脚本
-- 数据库名: zhiketong_db
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `zhiketong_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `zhiketong_db`;

-- -----------------------------------------------------------
-- 0. 班级表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `class` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `name`        VARCHAR(50) NOT NULL              COMMENT '班级名称',
  `teacher_id`  BIGINT      NOT NULL              COMMENT '班主任/创建教师ID',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- -----------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`   VARCHAR(50)  NOT NULL              COMMENT '用户名',
  `password`   VARCHAR(255) NOT NULL              COMMENT '密码',
  `role`       VARCHAR(20)  NOT NULL DEFAULT 'student' COMMENT '角色：student/teacher',
  `real_name`  VARCHAR(50)  DEFAULT NULL          COMMENT '真实姓名',
  `class_name` VARCHAR(50)  DEFAULT NULL          COMMENT '班级',
  `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_class_name` (`class_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 知识点表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `knowledge_point` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
  `name`       VARCHAR(100) NOT NULL              COMMENT '知识点名称',
  `parent_id`  BIGINT       DEFAULT NULL          COMMENT '父知识点ID',
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
  `type`               VARCHAR(20)  NOT NULL              COMMENT '题型：single/multi/true_false/fill_blank/short_answer',
  `title`              TEXT         NOT NULL              COMMENT '题干',
  `options`            JSON         DEFAULT NULL          COMMENT '选项JSON',
  `answer`             JSON         NOT NULL              COMMENT '正确答案JSON',
  `explanation`        TEXT         DEFAULT NULL          COMMENT '解析',
  `difficulty`         VARCHAR(10)  DEFAULT 'medium'      COMMENT '难度：easy/medium/hard',
  `knowledge_point_id` BIGINT       DEFAULT NULL          COMMENT '关联知识点ID',
  `created_at`         DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_kp` (`knowledge_point_id`),
  KEY `idx_difficulty` (`difficulty`),
  CONSTRAINT `fk_question_kp` FOREIGN KEY (`knowledge_point_id`)
    REFERENCES `knowledge_point` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库表';

-- -----------------------------------------------------------
-- 4. 用户答题记录表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_answer` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT        NOT NULL              COMMENT '用户ID',
  `question_id` BIGINT        NOT NULL              COMMENT '题目ID',
  `user_answer` VARCHAR(1000) DEFAULT NULL          COMMENT '用户提交答案',
  `is_correct`  TINYINT(1)    NOT NULL              COMMENT '是否正确',
  `answered_at` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_question` (`question_id`),
  KEY `idx_user_correct` (`user_id`, `is_correct`),
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
  `user_answer_id` BIGINT   NOT NULL              COMMENT '关联答题记录',
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
-- 6. 分层作业表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `homework` (
  `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '作业ID',
  `teacher_id`          BIGINT       NOT NULL              COMMENT '教师ID',
  `title`               VARCHAR(120) NOT NULL              COMMENT '作业标题',
  `description`         TEXT         DEFAULT NULL          COMMENT '作业说明',
  `knowledge_point_ids` JSON         NOT NULL              COMMENT '知识点ID列表',
  `question_ids`        JSON         DEFAULT NULL          COMMENT '题目ID列表',
  `difficulty`          VARCHAR(10)  NOT NULL              COMMENT '难度：easy/medium/hard/mixed',
  `class_id`            BIGINT       DEFAULT NULL          COMMENT '推送班级ID（关联class表）',
  `class_name`          VARCHAR(50)  DEFAULT NULL          COMMENT '推送班级名称（冗余）',
  `deadline`            DATETIME     DEFAULT NULL          COMMENT '截止时间',
  `created_at`          DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_teacher` (`teacher_id`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_class_name` (`class_name`),
  KEY `idx_difficulty` (`difficulty`),
  CONSTRAINT `fk_homework_teacher` FOREIGN KEY (`teacher_id`)
    REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分层作业表';

-- -----------------------------------------------------------
-- 7. 分层作业推送目标表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `homework_target` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '目标ID',
  `homework_id` BIGINT      NOT NULL              COMMENT '作业ID',
  `target_type` VARCHAR(20) NOT NULL              COMMENT '目标类型：class/student',
  `target_id`   BIGINT      DEFAULT NULL          COMMENT '学生ID',
  `class_name`  VARCHAR(50) DEFAULT NULL          COMMENT '班级名称',
  `created_at`  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_homework` (`homework_id`),
  KEY `idx_student` (`target_id`),
  KEY `idx_class_name` (`class_name`),
  CONSTRAINT `fk_homework_target_homework` FOREIGN KEY (`homework_id`)
    REFERENCES `homework` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分层作业推送目标表';
