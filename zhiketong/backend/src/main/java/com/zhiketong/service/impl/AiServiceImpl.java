package com.zhiketong.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiketong.dto.KnowledgeMasteryDto;
import com.zhiketong.dto.WrongRecordDetailDto;
import com.zhiketong.service.AiService;
import com.zhiketong.service.KnowledgeService;
import com.zhiketong.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private TeacherService teacherService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Override
    public SseEmitter chatStream(Long userId, String message, List<Map<String, String>> history) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5分钟超时

        new Thread(() -> {
            try {
                String systemPrompt = buildSystemPrompt(userId);

                // 构建 messages 数组
                List<Map<String, String>> messages = new ArrayList<>();
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);

                if (history != null) {
                    messages.addAll(history);
                }

                Map<String, String> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", message);
                messages.add(userMsg);

                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("messages", messages);
                requestBody.put("stream", true);
                requestBody.put("temperature", 0.7);
                requestBody.put("max_tokens", 2048);

                String jsonBody = objectMapper.writeValueAsString(requestBody);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .timeout(Duration.ofSeconds(120))
                        .build();

                HttpResponse<java.io.InputStream> response = httpClient.send(httpRequest,
                        HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    String errorBody = new String(response.body().readAllBytes());
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("AI 服务请求失败: " + response.statusCode() + " " + errorBody));
                    emitter.complete();
                    return;
                }

                // 逐行读取 SSE 流
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(response.body()))) {
                    String line;
                    StringBuilder fullContent = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6).trim();
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            try {
                                Map<String, Object> chunk = objectMapper.readValue(data,
                                        new TypeReference<Map<String, Object>>() {});
                                List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                    if (delta != null && delta.get("content") != null) {
                                        String content = (String) delta.get("content");
                                        fullContent.append(content);
                                        emitter.send(SseEmitter.event()
                                                .name("token")
                                                .data(content));
                                    }
                                }
                            } catch (Exception e) {
                                // 跳过解析失败的行
                            }
                        }
                    }

                    // 发送完成事件，附带完整内容
                    emitter.send(SseEmitter.event()
                            .name("done")
                            .data(fullContent.toString()));
                }

                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("AI 服务异常: " + e.getMessage()));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        }).start();

        return emitter;
    }

    @Override
    public String generateQuestions(Long userId, String subject, String mode, int count) {
        try {
            String context = buildStudentContextSummary(userId);

            String prompt = String.format(
                    "你是一位经验丰富的学科教师。请根据以下学生情况，生成 %d 道%s难度的%s选择题。\n\n" +
                    "学生学情:\n%s\n\n" +
                    "要求:\n" +
                    "1. 题目难度按模式调整: basic=基础巩固, variant=变式提升, challenge=拔高压轴\n" +
                    "2. 每道题包含: type(固定为\"单选题\"), source(知识点名称), stem(题干预HTML), " +
                    "options(选项数组，每项含key(A/B/C/D)和text), correctAnswer(正确答案字母), " +
                    "analysis(AI归因解析，解释为什么选这个答案)\n" +
                    "3. 针对学生的薄弱知识点出题\n" +
                    "4. 返回纯JSON数组格式，不要包含markdown代码块标记",
                    count, mode, subject, context);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", "你是一位专业的出题教师。请返回纯JSON数组，不要包含任何markdown标记或额外说明。");
            messages.add(sysMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.8);
            requestBody.put("max_tokens", 4096);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(120))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> result = objectMapper.readValue(response.body(),
                        new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) msg.get("content");
                    // 清理可能的 markdown 标记
                    content = content.trim();
                    if (content.startsWith("```")) {
                        content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
                    }
                    return content;
                }
            }
            return "[]";
        } catch (Exception e) {
            return "[]";
        }
    }

    @Override
    public Map<String, Object> getStudentContext(Long userId) {
        Map<String, Object> context = new HashMap<>();

        try {
            // 获取错题记录
            var studentDetail = teacherService.getStudentDetail(userId);
            if (studentDetail != null) {
                List<Map<String, String>> wrongSummary = new ArrayList<>();
                for (WrongRecordDetailDto w : studentDetail.getWrongList()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("question", w.getQuestionContent());
                    item.put("correctAnswer", w.getCorrectAnswer());
                    item.put("wrongAnswer", w.getWrongAnswer());
                    item.put("knowledgePoint", w.getKnowledgeName());
                    wrongSummary.add(item);
                }
                context.put("wrongRecords", wrongSummary);
            }

            // 获取薄弱知识点
            List<KnowledgeMasteryDto> masteryList = knowledgeService.getStudentMastery(userId);
            List<Map<String, Object>> weakPoints = masteryList.stream()
                    .filter(m -> m.getCorrectRate() < 0.6)
                    .map(m -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("knowledgeName", m.getKnowledgeName());
                        item.put("masteryRate", Math.round(m.getCorrectRate() * 100));
                        return item;
                    })
                    .collect(Collectors.toList());
            context.put("weakPoints", weakPoints);
        } catch (Exception e) {
            context.put("error", "获取上下文失败: " + e.getMessage());
        }

        return context;
    }

    /**
     * 构建引导式 system prompt，拒绝直接给答案
     */
    private String buildSystemPrompt(Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的启发式答疑助手，服务于中学生学习辅导。\n\n");
        prompt.append("## 核心原则\n");
        prompt.append("1. 绝对不要直接告诉学生答案！用苏格拉底式提问引导学生自己发现答案。\n");
        prompt.append("2. 先让学生说出自己的思路，再针对性引导。\n");
        prompt.append("3. 学生卡住时，把问题拆分成更小的步骤逐步引导。\n");
        prompt.append("4. 学生做对时，给予积极肯定并总结方法。\n");
        prompt.append("5. 学生做错时，不要批评，帮助他们分析原因。\n\n");

        // 注入学生上下文
        try {
            Map<String, Object> ctx = getStudentContext(userId);
            if (!ctx.containsKey("error")) {
                prompt.append("## 当前学生学情\n");
                List<Map<String, Object>> weakPoints = (List<Map<String, Object>>) ctx.get("weakPoints");
                if (weakPoints != null && !weakPoints.isEmpty()) {
                    prompt.append("薄弱知识点:\n");
                    for (Map<String, Object> wp : weakPoints) {
                        prompt.append("- ").append(wp.get("knowledgeName"))
                                .append(" (掌握度: ").append(wp.get("masteryRate")).append("%)\n");
                    }
                }
            }
        } catch (Exception e) {
            // 上下文获取失败时跳过
        }

        prompt.append("\n## 回答格式\n");
        prompt.append("用友好、鼓励的语气回复，适当使用emoji。");
        prompt.append("如果学生连续3次都无法理解，可以给更直接的提示但仍然不要给答案。");
        return prompt.toString();
    }

    private String buildStudentContextSummary(Long userId) {
        StringBuilder sb = new StringBuilder();
        try {
            Map<String, Object> ctx = getStudentContext(userId);
            List<Map<String, Object>> weakPoints = (List<Map<String, Object>>) ctx.get("weakPoints");
            if (weakPoints != null && !weakPoints.isEmpty()) {
                sb.append("薄弱知识点:\n");
                for (Map<String, Object> wp : weakPoints) {
                    sb.append("- ").append(wp.get("knowledgeName"))
                            .append(" (掌握度: ").append(wp.get("masteryRate")).append("%)\n");
                }
            } else {
                sb.append("暂无薄弱知识点数据\n");
            }
        } catch (Exception e) {
            sb.append("无法获取学生学情数据\n");
        }
        return sb.toString();
    }
}
