package com.zhiketong.backend.controller;

import com.zhiketong.backend.common.Result;
import com.zhiketong.backend.dto.ExercisePushRequestDTO;
import com.zhiketong.backend.dto.ExercisePushVO;
import com.zhiketong.backend.dto.ExerciseSubmitRequestDTO;
import com.zhiketong.backend.dto.ExerciseSubmitVO;
import com.zhiketong.backend.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/exercises")
@Tag(name = "练习业务", description = "根据薄弱点推送练习，并提交练习自动判题")
public class ExerciseController {

    @Resource
    private ExerciseService exerciseService;

    @PostMapping("/push")
    @Operation(summary = "练习推送", description = "根据学生薄弱知识点抽取题目，并按 easy/medium/hard 梯度组卷")
    public Result<ExercisePushVO> push(@RequestBody ExercisePushRequestDTO dto) {
        return Result.success(exerciseService.push(dto));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交练习", description = "提交学生答案，自动判题、保存答题记录，并将错题加入错题本")
    public Result<ExerciseSubmitVO> submit(@RequestBody ExerciseSubmitRequestDTO dto) {
        return Result.success(exerciseService.submit(dto));
    }
}
