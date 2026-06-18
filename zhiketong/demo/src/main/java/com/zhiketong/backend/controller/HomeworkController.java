package com.zhiketong.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiketong.backend.common.Result;
import com.zhiketong.backend.dto.HomeworkCreateDTO;
import com.zhiketong.backend.dto.HomeworkQuery;
import com.zhiketong.backend.dto.HomeworkVO;
import com.zhiketong.backend.entity.Homework;
import com.zhiketong.backend.entity.HomeworkSubmission;
import com.zhiketong.backend.mapper.HomeworkSubmissionMapper;
import com.zhiketong.backend.service.HomeworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/homeworks")
@Tag(name = "分层作业", description = "教师按知识点和难度发布作业到指定班级或学生")
public class HomeworkController {

    @Resource
    private HomeworkService homeworkService;

    @Resource
    private HomeworkSubmissionMapper submissionMapper;

    @PostMapping
    @Operation(summary = "发布分层作业", description = "教师选择知识点、难度，并推送到指定班级或指定学生")
    public Result<HomeworkVO> create(@RequestBody HomeworkCreateDTO dto) {
        return Result.success(homeworkService.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询作业详情", description = "根据作业ID查询作业和推送目标")
    public Result<HomeworkVO> detail(@PathVariable Long id) {
        return Result.success(homeworkService.getDetail(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询作业", description = "按教师、班级、难度分页查询作业")
    public Result<Page<Homework>> page(HomeworkQuery query) {
        return Result.success(homeworkService.page(query));
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "发布作业", description = "将草稿状态的作业发布，学生端可见")
    public Result<HomeworkVO> publish(@PathVariable Long id) {
        return Result.success(homeworkService.publish(id));
    }

    @GetMapping("/my")
    @Operation(summary = "学生查看作业", description = "根据班级名称查询已发布的作业")
    public Result<java.util.List<HomeworkVO>> myHomework(@RequestParam String className) {
        return Result.success(homeworkService.getByClassName(className));
    }

    @PostMapping("/submit")
    @Operation(summary = "学生提交作业", description = "提交作业答案")
    public Result<String> submit(@RequestBody java.util.Map<String, Object> body) {
        Long homeworkId = ((Number) body.get("homeworkId")).longValue();
        Long studentId = ((Number) body.get("studentId")).longValue();
        Object rawAnswers = body.get("answers");
        String answersJson;
        if (rawAnswers instanceof String) {
            try { answersJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(rawAnswers); }
            catch (Exception e) { answersJson = "\"\""; }
        } else {
            answersJson = "{}";
        }

        // 检查是否已提交
        Long count = submissionMapper.selectCount(new LambdaQueryWrapper<HomeworkSubmission>()
                .eq(HomeworkSubmission::getHomeworkId, homeworkId)
                .eq(HomeworkSubmission::getStudentId, studentId));
        if (count != null && count > 0) {
            return Result.error("已提交过该作业");
        }

        HomeworkSubmission sub = new HomeworkSubmission();
        sub.setHomeworkId(homeworkId);
        sub.setStudentId(studentId);
        sub.setAnswers(answersJson);
        sub.setScore(java.math.BigDecimal.ZERO);
        sub.setStatus("submitted");
        sub.setSubmittedAt(java.time.LocalDateTime.now());
        submissionMapper.insert(sub);
        return Result.success("提交成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除作业", description = "删除作业及其推送目标")
    public Result<Void> delete(@PathVariable Long id) {
        homeworkService.delete(id);
        return Result.success();
    }
}
