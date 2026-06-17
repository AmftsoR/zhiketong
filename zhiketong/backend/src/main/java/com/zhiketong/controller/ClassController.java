package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.entity.ClassEntity;
import com.zhiketong.service.ClassService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 班级相关接口
 */
@RestController
@RequestMapping("/api/class")
public class ClassController {

    @Autowired
    private ClassService classService;

    /**
     * 创建班级
     * POST /api/class/create
     * 请求体: { "className": "高三2班", "teacherId": 3 }
     */
    @PostMapping("/create")
    public R<String> createClass(@RequestBody Map<String, Object> body) {
        String className = (String) body.get("className");
        Long teacherId = Long.valueOf(body.get("teacherId").toString());
        boolean success = classService.createClass(className, teacherId);
        return success ? R.ok("创建成功") : R.fail("创建失败");
    }

    /**
     * 学生加入班级
     * POST /api/class/join
     * 请求体: { "studentId": 1, "classId": 1 }
     * 或从登录态获取 studentId
     */
    @PostMapping("/join")
    public R<String> joinClass(@RequestBody Map<String, Object> body,
                               HttpServletRequest request) {
        Long studentId = body.containsKey("studentId")
                ? Long.valueOf(body.get("studentId").toString())
                : (Long) request.getAttribute("userId");
        Long classId = Long.valueOf(body.get("classId").toString());
        boolean success = classService.joinClass(studentId, classId);
        return success ? R.ok("加入成功") : R.fail("加入失败，请检查学生或班级是否存在");
    }

    /**
     * 查看教师的所有班级
     * GET /api/class/teacher/{teacherId}
     */
    @GetMapping("/teacher/{teacherId}")
    public R<List<ClassEntity>> getTeacherClasses(@PathVariable Long teacherId) {
        List<ClassEntity> list = classService.getClassesByTeacher(teacherId);
        return R.ok(list);
    }
}
