# 智课通后端 API 测试文档

---

## 一、启动后端

### 1. 启动命令

```bash
cd D:\zhiketong\zhiketong\zhiketong\app_backend
mvn spring-boot:run
```

### 2. 确认启动成功

看到以下日志即成功：

```
Tomcat started on port(s): 8080 (http)
Started AppBackendApplication in X.XXX seconds
```

如果端口被占用，先杀掉旧进程：

```bash
# Windows PowerShell 查看端口占用
netstat -ano | findstr ":8080"

# 杀掉占用进程（将 PID 替换为实际值）
Stop-Process -Id <PID> -Force
```

### 3. 测试环境说明

| 项目 | 说明 |
|------|------|
| 激活的 profile | `dev`（H2 内存数据库） |
| 数据库 | H2（每次启动自动建表 + 插入测试数据） |
| 端口 | 8080 |
| H2 控制台 | http://localhost:8080/h2-console |

### 4. 预置测试数据

| 表 | 数据 |
|----|------|
| `app_user` | student1(张三,密码123,1班)、student2(李四,密码123,1班)、teacher1(王老师,密码123) |
| `class` | 高三(1)班(教师ID=3) |
| `knowledge_point` | 函数、三角函数、数列 |
| `question` | 3 道数学题 |
| `wrong_record` | 学生1错题：f(x)=x²的导数（答了x）、sin(π/2)（答了0） |
| `practice_record` | 学生1 3条练习（2对1错） |

---

## 二、API 测试方法（使用 curl）

### 测试 1：学生登录（正确密码）

**请求：**
```bash
curl -s -X POST "http://localhost:8080/api/user/login?username=student1&password=123"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "student1",
    "password": null,
    "name": "张三",
    "role": "student",
    "classId": 1,
    "createTime": "2026-06-13T19:31:24.566904"
  }
}
```

**验证点：**
- ✅ `code` = 200
- ✅ `password` 为 null（安全考虑，不返回密码）
- ✅ `role` = "student"
- ✅ `name` = "张三"
- ✅ `classId` = 1


### 测试 2：教师登录（正确密码）

**请求：**
```bash
curl -s -X POST "http://localhost:8080/api/user/login?username=teacher1&password=123"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3,
    "username": "teacher1",
    "password": null,
    "name": "王老师",
    "role": "teacher",
    "classId": null,
    "createTime": "2026-06-13T19:31:24.566904"
  }
}
```

**验证点：**
- ✅ `code` = 200
- ✅ `role` = "teacher"
- ✅ `classId` = null（教师没有班级ID）


### 测试 3：登录失败（错误密码）

**请求：**
```bash
curl -s -X POST "http://localhost:8080/api/user/login?username=student1&password=wrong"
```

**正确返回：**
```json
{
  "code": 500,
  "message": "用户名或密码错误",
  "data": null
}
```

**验证点：**
- ✅ `code` = 500（不是 200）
- ✅ `message` = "用户名或密码错误"
- ✅ `data` = null


### 测试 4：获取用户信息（学生 ID=1）

**请求：**
```bash
curl -s "http://localhost:8080/api/user/1"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "student1",
    "password": null,
    "name": "张三",
    "role": "student",
    "classId": 1,
    "createTime": "2026-06-13T19:31:24.566904"
  }
}
```

**验证点：**
- ✅ `code` = 200，`data` 不为 null


### 测试 5：获取用户信息（教师 ID=3）

**请求：**
```bash
curl -s "http://localhost:8080/api/user/3"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3,
    "username": "teacher1",
    "password": null,
    "name": "王老师",
    "role": "teacher",
    "classId": null,
    "createTime": "2026-06-13T19:31:24.566904"
  }
}
```


### 测试 6：获取教师的所有班级

**请求：**
```bash
curl -s "http://localhost:8080/api/class/teacher/3"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "高三(1)班",
      "teacherId": 3,
      "createTime": "2026-06-13T19:31:24.568986"
    }
  ]
}
```

**验证点：**
- ✅ `data` 是数组
- ✅ 数组中班级的 `teacherId` = 3
- ✅ 班级名是"高三(1)班"


### 测试 7：获取学生错题列表

**请求：**
```bash
curl -s "http://localhost:8080/api/wrong/list?studentId=1"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "wrongId": 1,
      "questionId": 1,
      "questionContent": "函数 f(x)=x^2 的导数是？",
      "correctAnswer": "2x",
      "wrongAnswer": "x",
      "knowledgeName": "函数",
      "wrongTime": "2026-06-13T19:31:24.569986",
      "imageUrl": null
    },
    {
      "wrongId": 2,
      "questionId": 2,
      "questionContent": "sin(π/2) 的值是？",
      "correctAnswer": "1",
      "wrongAnswer": "0",
      "knowledgeName": "三角函数",
      "wrongTime": "2026-06-13T19:31:24.569986",
      "imageUrl": null
    }
  ]
}
```

**验证点：**
- ✅ `code` = 200
- ✅ `data` 有 2 条记录
- ✅ 每条包含题目的正确答案（`correctAnswer`）和学生的错误答案（`wrongAnswer`）
- ✅ 关联了知识点名称（`knowledgeName`）
- ✅ 按错题时间倒序排列


### 测试 8：获取学生薄弱点知识图谱

**请求：**
```bash
curl -s "http://localhost:8080/api/knowledge/mastery?studentId=1"
```

**正确返回（学生1有练习记录时）：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "knowledgeId": 1,
      "knowledgeName": "函数",
      "correctRate": 1.0,
      "color": "green"
    },
    {
      "knowledgeId": 2,
      "knowledgeName": "三角函数",
      "correctRate": 0.0,
      "color": "red"
    },
    {
      "knowledgeId": 3,
      "knowledgeName": "数列",
      "correctRate": 1.0,
      "color": "green"
    }
  ]
}
```

**验证点：**
- ✅ `code` = 200
- ✅ `data` 包含所有知识点（3个）
- ✅ 每个知识点有 `correctRate`（0~1 之间）
- ✅ `color` 规则：
  - 正确率 ≥ 80% → `"green"`
  - 正确率 50%~80% → `"yellow"`
  - 正确率 < 50% → `"red"`
- ✅ 学生1的练习数据：函数 1/1=100%=green，三角函数 0/1=0%=red，数列 1/1=100%=green

**颜色逻辑对应测试数据：**

| 知识点 | 练习总数 | 正确数 | 正确率 | 颜色 |
|--------|---------|--------|--------|------|
| 函数 | 1 | 1 | 100% | green |
| 三角函数 | 1 | 0 | 0% | red |
| 数列 | 1 | 1 | 100% | green


### 测试 9：获取班级整体学情统计

**请求：**
```bash
curl -s "http://localhost:8080/api/teacher/class-statistics?teacherId=3"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "classId": 1,
      "className": "高三(1)班",
      "averageCorrectRate": 0.6666666666666666,
      "knowledgeAvgRates": null
    }
  ]
}
```

**验证点：**
- ✅ `code` = 200
- ✅ `data` 包含教师每个班级的统计
- ✅ `averageCorrectRate` = 2/3 ≈ 0.667（学生1 做了3题对2题）
- ✅ `className` 匹配


### 测试 10：获取单个学生完整学情详情

**请求：**
```bash
curl -s "http://localhost:8080/api/teacher/student-detail?studentId=1"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "studentId": 1,
    "studentName": "张三",
    "wrongList": [
      {
        "wrongId": 1,
        "questionId": 1,
        "questionContent": "函数 f(x)=x^2 的导数是？",
        "correctAnswer": "2x",
        "wrongAnswer": "x",
        "knowledgeName": "函数",
        "wrongTime": "2026-06-13T19:31:24.569986",
        "imageUrl": null
      },
      {
        "wrongId": 2,
        "questionId": 2,
        "questionContent": "sin(π/2) 的值是？",
        "correctAnswer": "1",
        "wrongAnswer": "0",
        "knowledgeName": "三角函数",
        "wrongTime": "2026-06-13T19:31:24.569986",
        "imageUrl": null
      }
    ],
    "masteryList": [
      {
        "knowledgeId": 1,
        "knowledgeName": "函数",
        "correctRate": 1.0,
        "color": "green"
      },
      {
        "knowledgeId": 2,
        "knowledgeName": "三角函数",
        "correctRate": 0.0,
        "color": "red"
      },
      {
        "knowledgeId": 3,
        "knowledgeName": "数列",
        "correctRate": 1.0,
        "color": "green"
      }
    ]
  }
}
```

**验证点：**
- ✅ `studentName` = "张三"
- ✅ 同时包含 `wrongList`（错题）和 `masteryList`（知识图谱）
- ✅ 两个子列表的内容分别与测试7和测试8一致


### 测试 11：创建班级

**请求：**
```bash
curl -s -X POST "http://localhost:8080/api/class/create?className=%E9%AB%98%E4%B8%892%E7%8F%AD&teacherId=3"
```

> 注意：`%E9%AB%98%E4%B8%892%E7%8F%AD` 是 "高三2班" 的 URL 编码，避免中文编码问题。

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**验证点：**
- ✅ `code` = 200
- ✅ `data` = true（创建成功）


### 测试 12：学生加入班级

**请求：**
```bash
curl -s -X POST "http://localhost:8080/api/class/join?studentId=1&classId=2"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**验证点：**
- ✅ `code` = 200
- ✅ `data` = true

**边界情况：**
- 学生不存在 → `{"code":500,"message":"加入失败，请检查学生或班级是否存在",...}`
- 班级不存在 → 同上
- 用户是教师（非学生）→ 同上


### 测试 13：注册新用户

**请求：**
```bash
curl -s -X POST -H "Content-Type: application/json;charset=UTF-8" \
  --data-raw "{\"username\":\"newuser\",\"password\":\"123\",\"name\":\"test\"}" \
  "http://localhost:8080/api/user/register"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```


### 测试 14：重复注册（用户名已存在）

**请求（同上，再发一次）：**
```bash
curl -s -X POST -H "Content-Type: application/json;charset=UTF-8" \
  --data-raw "{\"username\":\"newuser\",\"password\":\"123\",\"name\":\"test\"}" \
  "http://localhost:8080/api/user/register"
```

**正确返回：**
```json
{
  "code": 500,
  "message": "用户名已存在",
  "data": null
}
```

**验证点：**
- ✅ `code` = 500
- ✅ `message` = "用户名已存在"


### 测试 15：验证创建班级后列表变化

**请求：**
```bash
curl -s "http://localhost:8080/api/class/teacher/3"
```

**正确返回（应比测试6多一条）：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "高三(1)班",
      "teacherId": 3,
      "createTime": "..."
    },
    {
      "id": 2,
      "name": "高三2班",
      "teacherId": 3,
      "createTime": "..."
    }
  ]
}
```

**验证点：**
- ✅ 数组长度从 1 变为 2
- ✅ 新增班级 name="高三2班"，teacherId=3


### 测试 16：错题拍照上传（文件上传）

**请求（需要实际图片文件）：**
```bash
# 先创建一个测试图片文件
echo "test" > test_image.png

curl -s -X POST \
  -F "file=@test_image.png" \
  -F "studentId=1" \
  -F "questionContent=测试题目" \
  "http://localhost:8080/api/wrong/upload"
```

**正确返回：**
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**验证点：**
- ✅ `code` = 200
- ✅ 图片保存到 `uploads/` 目录
- ✅ 数据库记录 `is_uploaded` = 1, `image_url` 不为空

**边界情况：**
- 不传文件 → `{"code":500,"message":"文件不能为空",...}`

---

## 三、批量测试脚本

将以下内容保存为 `test_all.sh`，在 Git Bash 中运行：

```bash
#!/bin/bash
BASE="http://localhost:8080"
PASS=0
FAIL=0

check() {
  local name="$1"
  local expected="$2"
  local actual="$3"
  if echo "$actual" | grep -q "$expected"; then
    echo "  ✅ $name"
    PASS=$((PASS+1))
  else
    echo "  ❌ $name — 期望包含 '$expected'"
    echo "     返回: $actual"
    FAIL=$((FAIL+1))
  fi
}

echo "========== 开始 API 测试 =========="

# 1. 登录
R=$(curl -s -X POST "$BASE/api/user/login?username=student1&password=123")
check "学生登录" '"code":200' "$R"
check "学生登录-姓名" '张三' "$R"
check "学生登录-密码为null" '"password":null' "$R"

# 2. 教师登录
R=$(curl -s -X POST "$BASE/api/user/login?username=teacher1&password=123")
check "教师登录" '"code":200' "$R"
check "教师登录-角色" 'teacher' "$R"

# 3. 错误密码
R=$(curl -s -X POST "$BASE/api/user/login?username=student1&password=wrong")
check "错误密码" '用户名或密码错误' "$R"

# 4. 获取用户
R=$(curl -s "$BASE/api/user/1")
check "获取学生" '"code":200' "$R"

# 5. 获取教师
R=$(curl -s "$BASE/api/user/3")
check "获取教师" '"code":200' "$R"

# 6. 教师班级
R=$(curl -s "$BASE/api/class/teacher/3")
check "教师班级列表" '高三(1)班' "$R"

# 7. 错题列表
R=$(curl -s "$BASE/api/wrong/list?studentId=1")
check "错题列表" '"code":200' "$R"
check "错题-函数题" 'f(x)=x\^2' "$R"

# 8. 知识图谱
R=$(curl -s "$BASE/api/knowledge/mastery?studentId=1")
check "知识图谱" '"code":200' "$R"
check "知识图谱-红色" '"color":"red"' "$R"
check "知识图谱-绿色" '"color":"green"' "$R"

# 9. 班级统计
R=$(curl -s "$BASE/api/teacher/class-statistics?teacherId=3")
check "班级统计" '"code":200' "$R"

# 10. 学生详情
R=$(curl -s "$BASE/api/teacher/student-detail?studentId=1")
check "学生详情" '"studentName":"张三"' "$R"
check "学生详情-含错题" 'wrongList' "$R"
check "学生详情-含图谱" 'masteryList' "$R"

# 11. 创建班级
R=$(curl -s -X POST "$BASE/api/class/create?className=testclass&teacherId=3")
check "创建班级" 'true' "$R"

# 12. 加入班级
R=$(curl -s -X POST "$BASE/api/class/join?studentId=2&classId=2")
check "加入班级" 'true' "$R"

# 13. 注册
R=$(curl -s -X POST -H "Content-Type: application/json" \
  --data-raw '{"username":"tester1","password":"123","name":"测试"}' \
  "$BASE/api/user/register")
check "注册新用户" 'true' "$R"

# 14. 重复注册
R=$(curl -s -X POST -H "Content-Type: application/json" \
  --data-raw '{"username":"tester1","password":"123","name":"测试"}' \
  "$BASE/api/user/register")
check "重复注册被拒" '用户名已存在' "$R"

echo ""
echo "========== 测试完成: $PASS 通过, $FAIL 失败 =========="
```

---

## 四、数据库验证（可选）

访问 H2 控制台确认数据：http://localhost:8080/h2-console

| 参数 | 值 |
|------|-----|
| JDBC URL | `jdbc:h2:mem:zhiketong` |
| 用户名 | `sa` |
| 密码 | （留空） |

登录后可执行：

```sql
-- 查看所有用户
SELECT * FROM app_user;

-- 查看错题连表
SELECT u.name, q.content, w.wrong_answer, q.answer
FROM wrong_record w
JOIN app_user u ON w.user_id = u.id
JOIN question q ON w.question_id = q.id;

-- 查看练习正确率
SELECT u.name, kp.name, COUNT(*) as total,
       SUM(CASE WHEN pr.is_correct = 1 THEN 1 ELSE 0 END) as correct
FROM practice_record pr
JOIN app_user u ON pr.user_id = u.id
JOIN question q ON pr.question_id = q.id
JOIN knowledge_point kp ON q.knowledge_point_id = kp.id
GROUP BY u.name, kp.name;
```

---

## 五、API 总览表

| # | 方法 | 路径 | 功能 | 关键验证点 |
|---|------|------|------|-----------|
| 1 | POST | `/api/user/login` | 用户登录 | 成功返回200+用户信息(无密码)；失败返回500+错误提示 |
| 2 | POST | `/api/user/register` | 用户注册 | 成功返回true；重复用户名返回"用户名已存在" |
| 3 | GET | `/api/user/{id}` | 获取用户信息 | 返回用户信息(password=null) |
| 4 | POST | `/api/class/create` | 创建班级 | 返回true |
| 5 | POST | `/api/class/join` | 学生加入班级 | 返回true；无效参数返回错误提示 |
| 6 | GET | `/api/class/teacher/{teacherId}` | 教师班级列表 | 返回数组，每个班级有id/name/teacherId |
| 7 | GET | `/api/wrong/list` | 学生错题列表 | 返回数组，含题目内容+正确答案+错答+知识点 |
| 8 | POST | `/api/wrong/upload` | 拍照上传错题 | 返回true；图片保存到uploads/目录 |
| 9 | GET | `/api/knowledge/mastery` | 薄弱点知识图谱 | 返回所有知识点的正确率和颜色 |
| 10 | GET | `/api/teacher/class-statistics` | 班级整体学情 | 返回每个班级的平均正确率 |
| 11 | GET | `/api/teacher/student-detail` | 学生完整学情 | 同时返回错题列表+知识图谱 |

---

## 六、常见问题排查

| 问题 | 原因 | 解决 |
|------|------|------|
| 端口 8080 被占用 | 上次未正常关闭 | `netstat -ano \| findstr ":8080"` 查PID，`Stop-Process` 杀进程 |
| 所有 API 500 错误 | H2 数据库未初始化 | 检查 `application-dev.yml` 中 `spring.sql.init.mode=always` |
| user 表相关 API 报语法错误 | 表名 `user` 与保留字冲突 | 已修复：改名为 `app_user` |
| 知识图谱报 column not found | 表少列 | 已修复：`schema-h2.sql` 中补充 `parent_id` |
| 中文 JSON 400 错误 | curl 在 Windows 下的编码问题 | 使用 `--data-raw` + `charset=UTF-8`，或 URL 编码中文参数 |
