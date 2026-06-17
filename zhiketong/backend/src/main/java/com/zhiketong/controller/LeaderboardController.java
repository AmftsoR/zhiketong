package com.zhiketong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiketong.common.R;
import com.zhiketong.entity.User;
import com.zhiketong.entity.UserAnswer;
import com.zhiketong.mapper.UserAnswerMapper;
import com.zhiketong.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 班级排行榜接口
 */
@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAnswerMapper userAnswerMapper;

    /**
     * 获取班级排行榜（按正确率排序）
     * GET /api/leaderboard?classId=1&limit=10
     */
    @GetMapping
    public R<List<Map<String, Object>>> getLeaderboard(@RequestParam(required = false) Long classId,
                                                        @RequestParam(defaultValue = "20") int limit) {
        // 1. 获取班级所有学生
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, "student");
        if (classId != null) {
            userWrapper.eq(User::getClassId, classId);
        }
        List<User> students = userService.list(userWrapper);

        if (students.isEmpty()) {
            return R.ok(Collections.emptyList());
        }

        // 2. 查询所有学生的答题记录
        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        LambdaQueryWrapper<UserAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.in(UserAnswer::getUserId, studentIds);
        List<UserAnswer> allAnswers = userAnswerMapper.selectList(answerWrapper);

        // 3. 按学生分组计算正确率
        Map<Long, List<UserAnswer>> grouped = allAnswers.stream()
                .collect(Collectors.groupingBy(UserAnswer::getUserId));

        List<Map<String, Object>> leaderboard = new ArrayList<>();
        for (User student : students) {
            List<UserAnswer> answers = grouped.getOrDefault(student.getId(), Collections.emptyList());
            long total = answers.size();
            long correct = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
            double accuracy = total == 0 ? 0 : (double) correct / total;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("studentId", student.getId());
            entry.put("studentName", student.getRealName() != null ? student.getRealName() : student.getUsername());
            entry.put("className", student.getClassName());
            entry.put("totalAnswers", total);
            entry.put("accuracy", Math.round(accuracy * 10000.0) / 100.0); // 保留两位小数百分比
            leaderboard.add(entry);
        }

        // 按正确率降序
        leaderboard.sort((a, b) -> Double.compare((double) b.get("accuracy"), (double) a.get("accuracy")));

        // 返回 Top N
        return R.ok(leaderboard.subList(0, Math.min(limit, leaderboard.size())));
    }
}
