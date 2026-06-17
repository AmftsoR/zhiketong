package com.zhiketong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhiketong.entity.ClassEntity;
import java.util.List;

/**
 * 班级服务接口
 */
public interface ClassService extends IService<ClassEntity> {

    /**
     * 创建班级
     */
    boolean createClass(String className, Long teacherId);

    /**
     * 学生加入班级
     */
    boolean joinClass(Long studentId, Long classId);

    /**
     * 获取教师的所有班级
     */
    List<ClassEntity> getClassesByTeacher(Long teacherId);
}
