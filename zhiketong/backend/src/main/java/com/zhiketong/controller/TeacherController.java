package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.dto.ClassStatisticsDto;
import com.zhiketong.dto.StudentDetailDto;
import com.zhiketong.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端接口
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    /**
     * 获取教师所带班级的整体学情
     * GET /api/teacher/class-statistics?teacherId=3
     */
    @GetMapping("/class-statistics")
    public R<List<ClassStatisticsDto>> getClassStatistics(@RequestParam Long teacherId) {
        List<ClassStatisticsDto> list = teacherService.getClassStatistics(teacherId);
        return R.ok(list);
    }

    /**
     * 获取某个学生的完整学情详情
     * GET /api/teacher/student-detail?studentId=1
     */
    @GetMapping("/student-detail")
    public R<StudentDetailDto> getStudentDetail(@RequestParam Long studentId) {
        StudentDetailDto detail = teacherService.getStudentDetail(studentId);
        if (detail == null) {
            return R.fail("学生不存在或不是学生角色");
        }
        return R.ok(detail);
    }
}
