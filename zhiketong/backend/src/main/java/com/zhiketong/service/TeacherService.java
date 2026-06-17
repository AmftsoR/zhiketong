package com.zhiketong.service;

import com.zhiketong.dto.ClassStatisticsDto;
import com.zhiketong.dto.StudentDetailDto;
import java.util.List;

/**
 * 教师端服务接口
 */
public interface TeacherService {

    /**
     * 获取教师所带班级的整体学情统计
     */
    List<ClassStatisticsDto> getClassStatistics(Long teacherId);

    /**
     * 获取某个学生的完整学情详情
     */
    StudentDetailDto getStudentDetail(Long studentId);
}
