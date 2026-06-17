-- =============================================
-- 智课通 测试数据 (MySQL)
-- =============================================

-- 用户（两个学生，一个教师）
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `real_name`, `role`, `class_name`, `class_id`) VALUES
(1, 'student1', '123', '张三', 'student', '高三(1)班', 1),
(2, 'student2', '123', '李四', 'student', '高三(1)班', 1),
(3, 'teacher1', '123', '王老师', 'teacher', NULL, NULL);

-- 班级
INSERT IGNORE INTO `class` (`id`, `name`, `teacher_id`) VALUES
(1, '高三(1)班', 3);

-- 知识点（数学）
INSERT IGNORE INTO `knowledge_point` (`id`, `name`, `subject`) VALUES
(1, '函数', '数学'),
(2, '三角函数', '数学'),
(3, '数列', '数学');

-- 题目
INSERT IGNORE INTO `question_bank` (`id`, `type`, `title`, `options`, `answer`, `explanation`, `difficulty`, `knowledge_point_id`) VALUES
(1, 'blank', '函数 f(x)=x² 的导数是？', NULL, '2x', '根据幂函数求导公式，(xⁿ)′ = n·xⁿ⁻¹，所以 f′(x)=2x', 'easy', 1),
(2, 'blank', 'sin(π/2) 的值是？', NULL, '1', 'sin(π/2) = 1，这是三角函数的基本值', 'easy', 2),
(3, 'blank', '等差数列的通项公式是？', NULL, 'a1+(n-1)d', '等差数列通项公式：aₙ = a₁ + (n-1)d，其中 a₁ 为首项，d 为公差', 'medium', 3);

-- 练习记录（用于计算正确率）
INSERT IGNORE INTO `practice_record` (`id`, `user_id`, `question_id`, `answer`, `is_correct`) VALUES
(1, 1, 1, '2x', 1),
(2, 1, 2, '0', 0),
(3, 1, 3, 'a1+(n-1)d', 1);

-- 错题记录（学生1有两道错题）
INSERT IGNORE INTO `wrong_record` (`id`, `user_id`, `question_id`, `wrong_answer`) VALUES
(1, 1, 1, 'x'),
(2, 1, 2, '0');
