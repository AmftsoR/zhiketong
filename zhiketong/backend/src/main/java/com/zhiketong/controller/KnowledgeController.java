package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.dto.KnowledgeMasteryDto;
import com.zhiketong.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识点掌握度接口
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    /**
     * 获取学生的薄弱点图谱数据
     * GET /api/knowledge/mastery?studentId=1
     */
    @GetMapping("/mastery")
    public R<List<KnowledgeMasteryDto>> getMastery(@RequestParam Long studentId) {
        List<KnowledgeMasteryDto> list = knowledgeService.getStudentMastery(studentId);
        return R.ok(list);
    }
}
