-- 插入两个学生，一个教师
INSERT INTO `app_user` (`username`, `password`, `name`, `role`, `class_id`) VALUES
('student1', '123', '张三', 'student', 1),
('student2', '123', '李四', 'student', 1),
('teacher1', '123', '王老师', 'teacher', NULL);

-- 插入班级
INSERT INTO `class` (`id`, `name`, `teacher_id`) VALUES
(1, '高三(1)班', 3);

-- 插入知识点（数学）
INSERT INTO `knowledge_point` (`id`, `name`, `subject`) VALUES
(1, '函数', '数学'),
(2, '三角函数', '数学'),
(3, '数列', '数学');

-- 插入题目（关联知识点）
INSERT INTO `question` (`id`, `content`, `answer`, `knowledge_point_id`, `difficulty`) VALUES
(1, '函数 f(x)=x^2 的导数是？', '2x', 1, 'easy'),
(2, 'sin(π/2) 的值是？', '1', 2, 'easy'),
(3, '等差数列的通项公式是？', 'a1+(n-1)d', 3, 'medium');

-- 插入错题记录（学生1有两道错题）
INSERT INTO `wrong_record` (`user_id`, `question_id`, `wrong_answer`) VALUES
(1, 1, 'x'),
(1, 2, '0');

-- 插入练习记录（用于计算正确率）
INSERT INTO `practice_record` (`user_id`, `question_id`, `answer`, `is_correct`) VALUES
(1, 1, '2x', 1),
(1, 2, '0', 0),
(1, 3, 'a1+(n-1)d', 1);