package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.service.AiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * AI 助手控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * 流式聊天（SSE）
     * POST /api/ai/chat
     * 请求体: { message: "...", history: [...] }
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody Map<String, Object> payload,
                           HttpServletRequest request) {
        Long userId = resolveUserId(request);
        String message = (String) payload.getOrDefault("message", "");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) payload.get("history");
        return aiService.chatStream(userId, message, history);
    }

    /**
     * 生成靶向练习题
     * POST /api/ai/generate-questions
     * 请求体: { subject: "math", mode: "basic", count: 3 }
     */
    @PostMapping("/generate-questions")
    public R<String> generateQuestions(@RequestBody Map<String, Object> payload,
                                        HttpServletRequest request) {
        Long userId = resolveUserId(request);
        String subject = (String) payload.getOrDefault("subject", "math");
        String mode = (String) payload.getOrDefault("mode", "basic");
        int count = payload.get("count") instanceof Number
                ? ((Number) payload.get("count")).intValue()
                : 3;
        String questions = aiService.generateQuestions(userId, subject, mode, count);
        return R.ok(questions);
    }

    /**
     * 获取学生AI上下文
     * GET /api/ai/context
     */
    @GetMapping("/context")
    public R<Map<String, Object>> getContext(HttpServletRequest request) {
        Long userId = resolveUserId(request);
        Map<String, Object> context = aiService.getStudentContext(userId);
        return R.ok(context);
    }

    private Long resolveUserId(HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("userId");
        if (loginUserId != null) return loginUserId;
        return 1L;
    }
}
