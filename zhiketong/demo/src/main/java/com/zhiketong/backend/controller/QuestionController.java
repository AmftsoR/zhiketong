package com.zhiketong.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiketong.backend.common.Result;
import com.zhiketong.backend.dto.QuestionDTO;
import com.zhiketong.backend.dto.QuestionImportResultVO;
import com.zhiketong.backend.dto.QuestionQuery;
import com.zhiketong.backend.entity.Question;
import com.zhiketong.backend.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "题库管理", description = "题目增删改查、分页查询、Excel批量导入")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @PostMapping
    @Operation(summary = "新增题目", description = "创建单个题目，支持知识点、题型、难度、答案和解析")
    public Result<Question> create(@RequestBody QuestionDTO dto) {
        return Result.success(questionService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改题目", description = "根据题目ID修改题库内容")
    public Result<Question> update(@PathVariable Long id, @RequestBody QuestionDTO dto) {
        dto.setId(id);
        return Result.success(questionService.update(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目", description = "根据题目ID删除题目")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询题目详情", description = "根据题目ID查询题目详情")
    public Result<Question> getById(@PathVariable Long id) {
        return Result.success(questionService.getById(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询题目", description = "按知识点、难度、题型、关键词分页查询题目")
    public Result<Page<Question>> page(QuestionQuery query) {
        return Result.success(questionService.page(query));
    }

    @PostMapping("/import")
    @Operation(summary = "Excel批量导入题目", description = "上传Excel文件批量导入题库")
    public Result<QuestionImportResultVO> importExcel(@RequestParam("file") MultipartFile file) {
        return Result.success(questionService.importExcel(file));
    }
}
