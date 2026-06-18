package com.zhiketong.backend.controller;

import com.zhiketong.backend.common.Result;
import com.zhiketong.backend.dto.TeachingReportRequestDTO;
import com.zhiketong.backend.dto.TeachingReportVO;
import com.zhiketong.backend.service.TeachingReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/teaching-reports")
@Tag(name = "AI教研报告", description = "基于学生练习数据生成教研报告，支持 DeepSeek")
public class TeachingReportController {

    @Resource
    private TeachingReportService teachingReportService;

    @PostMapping("/generate")
    @Operation(summary = "生成AI教研报告", description = "统计知识点正确率并生成教研建议；配置 DEEPSEEK_API_KEY 后调用 DeepSeek")
    public Result<TeachingReportVO> generate(@RequestBody TeachingReportRequestDTO dto) {
        return Result.success(teachingReportService.generate(dto));
    }
}
