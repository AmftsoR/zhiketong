package com.zhiketong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiketong.dto.ClassStatisticsDto;
import com.zhiketong.dto.KnowledgeMasteryDto;
import com.zhiketong.dto.StudentDetailDto;
import com.zhiketong.dto.WrongRecordDetailDto;
import com.zhiketong.entity.ClassEntity;
import com.zhiketong.entity.User;
import com.zhiketong.entity.UserAnswer;
import com.zhiketong.mapper.MistakeBookMapper;
import com.zhiketong.mapper.UserAnswerMapper;
import com.zhiketong.service.ClassService;
import com.zhiketong.service.KnowledgeService;
import com.zhiketong.service.TeacherService;
import com.zhiketong.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private ClassService classService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAnswerMapper userAnswerMapper;

    @Autowired
    private MistakeBookMapper mistakeBookMapper;

    @Autowired
    private KnowledgeService knowledgeService;

    @Override
    public List<ClassStatisticsDto> getClassStatistics(Long teacherId) {
        // 1. 获取该教师的所有班级
        List<ClassEntity> classes = classService.getClassesByTeacher(teacherId);
        List<ClassStatisticsDto> result = new ArrayList<>();

        for (ClassEntity clazz : classes) {
            // 2. 获取班级所有学生
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(User::getClassId, clazz.getId()).eq(User::getRole, "student");
            List<User> students = userService.list(userWrapper);

            ClassStatisticsDto dto = new ClassStatisticsDto();
            dto.setClassId(clazz.getId());
            dto.setClassName(clazz.getName());

            if (students.isEmpty()) {
                dto.setAverageCorrectRate(0.0);
                result.add(dto);
                continue;
            }

            // 3. 计算班级平均正确率
            List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
            LambdaQueryWrapper<UserAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.in(UserAnswer::getUserId, studentIds);
            List<UserAnswer> answers = userAnswerMapper.selectList(answerWrapper);

            if (answers.isEmpty()) {
                dto.setAverageCorrectRate(0.0);
            } else {
                long total = answers.size();
                long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
                dto.setAverageCorrectRate(total == 0 ? 0 : (double) correctCount / total);
            }
            result.add(dto);
        }
        return result;
    }

    @Override
    public StudentDetailDto getStudentDetail(Long studentId) {
        User student = userService.getById(studentId);
        if (student == null || !"student".equals(student.getRole())) {
            return null;
        }

        StudentDetailDto dto = new StudentDetailDto();
        dto.setStudentId(studentId);
        dto.setStudentName(student.getRealName() != null ? student.getRealName() : student.getUsername());

        // 错题本详情（连表查询）
        List<WrongRecordDetailDto> wrongList = mistakeBookMapper.selectWrongDetailByUserId(studentId);
        dto.setWrongList(wrongList);

        // 薄弱点图谱
        List<KnowledgeMasteryDto> masteryList = knowledgeService.getStudentMastery(studentId);
        dto.setMasteryList(masteryList);

        return dto;
    }
}
