package com.zhiketong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhiketong.entity.ClassEntity;
import com.zhiketong.entity.User;
import com.zhiketong.mapper.ClassMapper;
import com.zhiketong.service.ClassService;
import com.zhiketong.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, ClassEntity> implements ClassService {

    @Autowired
    private UserService userService;

    @Override
    public boolean createClass(String className, Long teacherId) {
        ClassEntity clazz = new ClassEntity();
        clazz.setName(className);
        clazz.setTeacherId(teacherId);
        return this.save(clazz);
    }

    @Override
    @Transactional
    public boolean joinClass(Long studentId, Long classId) {
        User student = userService.getById(studentId);
        if (student == null || !"student".equals(student.getRole())) {
            return false;
        }
        ClassEntity clazz = this.getById(classId);
        if (clazz == null) {
            return false;
        }
        student.setClassId(classId);
        student.setClassName(clazz.getName());
        return userService.updateById(student);
    }

    @Override
    public List<ClassEntity> getClassesByTeacher(Long teacherId) {
        LambdaQueryWrapper<ClassEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassEntity::getTeacherId, teacherId)
               .orderByDesc(ClassEntity::getCreateTime);
        return this.list(wrapper);
    }
}
