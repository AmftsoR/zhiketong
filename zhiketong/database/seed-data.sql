-- =============================================
-- 知课堂 完整种子数据 (BCrypt 密码)
-- 密码: 123456 -> $2b$10$1wW7ACK1qcee5PfbX8e4xu4l9YBa931pa7FsogSKcUmsC.mTNQ3Ue
-- =============================================
USE `zhiketong_db`;

-- 清理旧数据
DELETE FROM `mistake_book`;
DELETE FROM `user_answer`;
DELETE FROM `user`;
DELETE FROM `class`;
DELETE FROM `knowledge_point`;
DELETE FROM `question_bank`;

-- 班级
INSERT INTO `class` (`id`, `name`, `teacher_id`) VALUES
(1, '高三(1)班', 3);

-- 用户 (BCrypt 密码 = 123456)
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `role`, `class_name`, `class_id`) VALUES
(1, 'student1', '$2b$10$1wW7ACK1qcee5PfbX8e4xu4l9YBa931pa7FsogSKcUmsC.mTNQ3Ue', '张三', 'student', '高三(1)班', 1),
(2, 'student2', '$2b$10$1wW7ACK1qcee5PfbX8e4xu4l9YBa931pa7FsogSKcUmsC.mTNQ3Ue', '李四', 'student', '高三(1)班', 1),
(3, 'teacher1', '$2b$10$1wW7ACK1qcee5PfbX8e4xu4l9YBa931pa7FsogSKcUmsC.mTNQ3Ue', '王老师', 'teacher', NULL, NULL);

-- 知识点
INSERT INTO `knowledge_point` (`id`, `name`, `subject`) VALUES
(1, '函数', 'math'),
(2, '三角函数', 'math'),
(3, '数列', 'math');

-- 题库 (answer 为 JSON 数组格式)
INSERT INTO `question_bank` (`id`, `type`, `title`, `options`, `answer`, `explanation`, `difficulty`, `knowledge_point_id`) VALUES
(1, 'single_choice', '函数 f(x)=x² 的导数是？',
    '["x", "2x", "x²", "2"]',
    '["2x"]',
    '根据幂函数求导公式，(xⁿ)′ = n·xⁿ⁻¹，所以 f′(x)=2x',
    'easy', 1),

(2, 'single_choice', 'sin(π/2) 的值是？',
    '["0", "1", "-1", "1/2"]',
    '["1"]',
    'sin(π/2) = 1，这是三角函数的基本值',
    'easy', 2),

(3, 'single_choice', '等差数列的通项公式是？',
    '["a1+(n-1)d", "a1·r^(n-1)", "a1/(1-r)", "a1+d"]',
    '["a1+(n-1)d"]',
    '等差数列通项公式：aₙ = a₁ + (n-1)d，其中 a₁ 为首项，d 为公差',
    'medium', 3);

-- 答题记录 (用户1的练习历史：2对1错)
INSERT INTO `user_answer` (`id`, `user_id`, `question_id`, `user_answer`, `is_correct`) VALUES
(1, 1, 1, 'B', 1),
(2, 1, 2, 'A', 0),
(3, 1, 3, 'A', 1);

-- 错题本 (用户1有2道错题)
INSERT INTO `mistake_book` (`id`, `user_id`, `question_id`, `user_answer_id`) VALUES
(1, 1, 1, 1),
(2, 1, 2, 2);
