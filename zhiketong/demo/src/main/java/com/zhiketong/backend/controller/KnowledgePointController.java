package com.zhiketong.backend.controller;

import com.zhiketong.backend.common.Result;
import com.zhiketong.backend.dto.KnowledgePointCreateDTO;
import com.zhiketong.backend.dto.KnowledgePointGenerateDTO;
import com.zhiketong.backend.dto.KnowledgePointMoveDTO;
import com.zhiketong.backend.dto.KnowledgePointTreeVO;
import com.zhiketong.backend.dto.KnowledgePointUpdateDTO;
import com.zhiketong.backend.entity.KnowledgePoint;
import com.zhiketong.backend.service.KnowledgePointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge-points")
@Tag(name = "知识点管理", description = "树形知识点查询、创建、修改、删除和移动")
public class KnowledgePointController {

    @Resource
    private KnowledgePointService knowledgePointService;

    @GetMapping("/tree")
    @Operation(summary = "查询知识点树", description = "按 parentId 组织知识点树形结构")
    public Result<List<KnowledgePointTreeVO>> tree() {
        return Result.success(knowledgePointService.getTree());
    }

    @PostMapping
    @Operation(summary = "新增知识点", description = "创建知识点，可指定父级知识点形成树形结构")
    public Result<KnowledgePoint> create(@Valid @RequestBody KnowledgePointCreateDTO dto) {
        return Result.success(knowledgePointService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改知识点", description = "根据知识点ID修改名称或学科")
    public Result<KnowledgePoint> update(@PathVariable Long id, @RequestBody KnowledgePointUpdateDTO dto) {
        dto.setId(id);
        return Result.success(knowledgePointService.update(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除知识点", description = "删除无子节点的知识点")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgePointService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/move")
    @Operation(summary = "移动知识点", description = "修改知识点父级节点，实现树节点移动")
    public Result<Void> move(@PathVariable Long id, @RequestBody KnowledgePointMoveDTO dto) {
        knowledgePointService.move(id, dto);
        return Result.success();
    }

    @PostMapping("/generate")
    @Operation(summary = "AI 生成知识点", description = "调用 DeepSeek 根据学科和主题生成知识点树")
    public Result<List<KnowledgePointTreeVO>> generate(@Valid @RequestBody KnowledgePointGenerateDTO dto) {
        return Result.success(knowledgePointService.generate(dto));
    }
}
