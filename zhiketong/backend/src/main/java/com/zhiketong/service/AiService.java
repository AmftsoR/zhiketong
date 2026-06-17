package com.zhiketong.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * AI 服务接口
 */
public interface AiService {

    /**
     * 流式聊天
     * @param userId 学生ID
     * @param message 用户消息
     * @param history 对话历史
     * @return SseEmitter 用于流式推送
     */
    SseEmitter chatStream(Long userId, String message, List<Map<String, String>> history);

    /**
     * 生成靶向练习题
     * @param userId 学生ID
     * @param subject 科目
     * @param mode 难度
     * @param count 题目数量
     * @return 生成的题目列表（JSON字符串）
     */
    String generateQuestions(Long userId, String subject, String mode, int count);

    /**
     * 获取学生AI上下文（错题、薄弱点）
     */
    Map<String, Object> getStudentContext(Long userId);
}
