package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.entity.KnowledgePoint;
import com.zhiketong.entity.MistakeBook;
import com.zhiketong.entity.QuestionBank;
import com.zhiketong.entity.UserAnswer;
import com.zhiketong.mapper.KnowledgePointMapper;
import com.zhiketong.mapper.MistakeBookMapper;
import com.zhiketong.mapper.QuestionBankMapper;
import com.zhiketong.mapper.UserAnswerMapper;
import com.zhiketong.service.MistakeService;
import com.zhiketong.vo.MistakeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/mistakes")
public class MistakeController {

    @Autowired
    private MistakeService mistakeService;
    @Autowired
    private QuestionBankMapper questionBankMapper;
    @Autowired
    private UserAnswerMapper userAnswerMapper;
    @Autowired
    private MistakeBookMapper mistakeBookMapper;
    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    /**
     * 获取当前用户的错题列表
     * GET /api/mistakes?userId=1
     * 如已登录，优先使用登录用户 ID
     */
    @GetMapping
    public R<List<MistakeVO>> listMistakes(@RequestParam(required = false) Long userId,
                                            HttpServletRequest request) {
        Long uid = resolveUserId(userId, request);
        List<MistakeVO> list = mistakeService.getMistakesByUser(uid);
        return R.ok(list);
    }

    /**
     * 手动添加错题
     * POST /api/mistakes
     * 请求体: {userId, subject, type, difficulty, stem, options, correctAnswer, myAnswer, analysis}
     * 如已登录，优先使用登录用户 ID
     */
    @PostMapping
    public R<String> addMistake(@RequestBody Map<String, Object> payload,
                                 HttpServletRequest request) {
        Long userId = payload.get("userId") instanceof Number
                ? ((Number) payload.get("userId")).longValue()
                : null;
        userId = resolveUserId(userId, request);

        String stem = (String) payload.get("stem");
        String type = (String) payload.getOrDefault("type", "single");
        String difficulty = (String) payload.getOrDefault("difficulty", "medium");
        String options = (String) payload.getOrDefault("options", "[]");
        String correctAnswer = (String) payload.get("correctAnswer");
        String myAnswer = (String) payload.get("myAnswer");
        String analysis = (String) payload.getOrDefault("analysis", "");
        String subject = (String) payload.get("subject");

        if (stem == null || stem.isBlank()) {
            return R.fail("题干不能为空");
        }
        if (correctAnswer == null || correctAnswer.isBlank()) {
            return R.fail("正确答案不能为空");
        }

        // 查找匹配科目的知识点，关联到题目
        Long knowledgePointId = null;
        if (subject != null && !subject.isBlank()) {
            LambdaQueryWrapper<KnowledgePoint> kpWrapper = new LambdaQueryWrapper<>();
            kpWrapper.eq(KnowledgePoint::getSubject, subject).last("LIMIT 1");
            KnowledgePoint kp = knowledgePointMapper.selectOne(kpWrapper);
            if (kp != null) {
                knowledgePointId = kp.getId();
            }
        }

        // 1. 创建题目（answer 列是 JSON 类型，需包装为 JSON 数组）
        QuestionBank question = new QuestionBank();
        question.setType(type);
        question.setTitle(stem);
        // options 也需保证是合法 JSON，若为空则用 "[]"
        String safeOptions = (options == null || options.isBlank()) ? "[]" : options;
        question.setOptions(safeOptions);
        // answer 列是 JSON 类型，存为 ["A"] 格式
        String jsonAnswer = "[\"" + correctAnswer + "\"]";
        question.setAnswer(jsonAnswer);
        question.setExplanation(analysis);
        question.setDifficulty(difficulty);
        question.setKnowledgePointId(knowledgePointId);
        question.setCreatedAt(LocalDateTime.now());
        questionBankMapper.insert(question);

        // 2. 创建答题记录（标记为错误）
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUserId(userId);
        userAnswer.setQuestionId(question.getId());
        userAnswer.setUserAnswer(myAnswer != null ? myAnswer : "");
        userAnswer.setIsCorrect(false);
        userAnswer.setAnsweredAt(LocalDateTime.now());
        userAnswerMapper.insert(userAnswer);

        // 3. 加入错题本
        MistakeBook mistake = new MistakeBook();
        mistake.setUserId(userId);
        mistake.setQuestionId(question.getId());
        mistake.setUserAnswerId(userAnswer.getId());
        mistake.setAddedAt(LocalDateTime.now());
        mistakeBookMapper.insert(mistake);

        return R.ok("错题添加成功");
    }

    /**
     * 删除某道错题
     * DELETE /api/mistakes/{questionId}
     */
    @DeleteMapping("/{questionId}")
    public R<String> removeMistake(@PathVariable Long questionId,
                                   @RequestParam(required = false) Long userId,
                                   HttpServletRequest request) {
        Long uid = resolveUserId(userId, request);
        mistakeService.removeMistake(uid, questionId);
        return R.ok("删除成功");
    }

    /**
     * 拍照上传错题
     * POST /api/mistakes/upload
     * multipart/form-data: file, studentId(可选)
     */
    @PostMapping("/upload")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file,
                                  @RequestParam(required = false) Long userId,
                                  HttpServletRequest request) {
        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }
        Long uid = resolveUserId(userId, request);

        try {
            // 保存到 uploads 目录
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID() + ext;
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            // 创建错题记录（is_uploaded 标记通过 question_id = 0 表示纯图片错题）
            MistakeBook mistake = new MistakeBook();
            mistake.setUserId(uid);
            mistake.setQuestionId(0L); // 0 表示无关联题目
            mistake.setUserAnswerId(0L);
            mistake.setAddedAt(LocalDateTime.now());
            mistakeBookMapper.insert(mistake);

            return R.ok("/uploads/" + fileName);
        } catch (IOException e) {
            return R.fail("文件保存失败");
        }
    }

    /**
     * 解析用户 ID：优先从登录态获取，其次用请求参数，最后 fallback 到 1
     */
    private Long resolveUserId(Long paramUserId, HttpServletRequest request) {
        if (paramUserId != null) return paramUserId;
        Long loginUserId = (Long) request.getAttribute("userId");
        if (loginUserId != null) return loginUserId;
        return 1L; // fallback
    }
}
