package com.zhiketong.controller;

import com.zhiketong.entity.MistakeBook;
import com.zhiketong.service.MistakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController  // 标记这是一个 REST 控制器
@RequestMapping("/api/mistakes")  // 所有接口都以 /api/mistakes 开头
public class MistakeController {

    @Autowired
    private MistakeService mistakeService;

    /**
     * 获取当前用户的错题列表
     * GET /api/mistakes
     */
    @GetMapping
    public List<MistakeBook> listMistakes() {
        // TODO: 实际项目中应该从登录信息中获取用户ID，这里暂时写死为1（测试用）
        Long userId = 1L;
        return mistakeService.getMistakesByUser(userId);
    }

    /**
     * 删除某道错题
     * DELETE /api/mistakes/{questionId}
     */
    @DeleteMapping("/{questionId}")
    public String removeMistake(@PathVariable Long questionId) {
        Long userId = 1L;
        mistakeService.removeMistake(userId, questionId);
        return "删除成功";
    }
}