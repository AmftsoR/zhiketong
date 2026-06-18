-- ============================================================
-- 知识点种子数据（名称精简版，2-3字为主）
-- 数据库: zhiketong_db
-- 注意: 使用固定ID，如与现有数据冲突请先清空 knowledge_point 表
-- ============================================================

USE `zhiketong_db`;

-- 数学知识点 (ID: 1-10)
INSERT INTO knowledge_point (id, name, parent_id, subject) VALUES
(1,  '集合', NULL, 'math'),
(2,  '函数', NULL, 'math'),
(3,  '三角', NULL, 'math'),
(4,  '数列', NULL, 'math'),
(5,  '不等式', NULL, 'math'),
(6,  '向量', NULL, 'math'),
(7,  '立体几何', NULL, 'math'),
(8,  '解析几何', NULL, 'math'),
(9,  '导数', NULL, 'math'),
(10, '概率', NULL, 'math');

-- 物理知识点 (ID: 11-20)
INSERT INTO knowledge_point (id, name, parent_id, subject) VALUES
(11, '运动', NULL, 'physics'),
(12, '牛顿定律', NULL, 'physics'),
(13, '曲线运动', NULL, 'physics'),
(14, '万有引力', NULL, 'physics'),
(15, '机械能', NULL, 'physics'),
(16, '电场', NULL, 'physics'),
(17, '电流', NULL, 'physics'),
(18, '磁场', NULL, 'physics'),
(19, '电磁感应', NULL, 'physics'),
(20, '热学', NULL, 'physics');

-- 英语知识点 (ID: 21-30)
INSERT INTO knowledge_point (id, name, parent_id, subject) VALUES
(21, '语法', NULL, 'english'),
(22, '完形', NULL, 'english'),
(23, '阅读', NULL, 'english'),
(24, '七选五', NULL, 'english'),
(25, '改错', NULL, 'english'),
(26, '写作', NULL, 'english'),
(27, '词汇', NULL, 'english'),
(28, '时态语态', NULL, 'english'),
(29, '从句', NULL, 'english'),
(30, '非谓语', NULL, 'english');
