package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.entity.MistakeBook;
import com.zhiketong.entity.QuestionBank;
import com.zhiketong.entity.UserAnswer;
import com.zhiketong.mapper.MistakeBookMapper;
import com.zhiketong.mapper.QuestionBankMapper;
import com.zhiketong.mapper.UserAnswerMapper;
import com.zhiketong.service.MistakeService;
import com.zhiketong.vo.MistakeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * 获取当前用户的错题列表
     * GET /api/mistakes
     */
    @GetMapping
    public R<List<MistakeVO>> listMistakes() {
        Long userId = 1L; // TODO: 从登录信息获取
        List<MistakeVO> list = mistakeService.getMistakesByUser(userId);
        return R.ok(list);
    }

    /**
     * 手动添加错题
     * POST /api/mistakes
     * 请求体: {subject, type, difficulty, stem, options, correctAnswer, myAnswer, analysis}
     */
    @PostMapping
    public R<String> addMistake(@RequestBody Map<String, Object> payload) {
        Long userId = 1L; // TODO: 从登录信息获取

        String stem = (String) payload.get("stem");
        String type = (String) payload.getOrDefault("type", "single");
        String difficulty = (String) payload.getOrDefault("difficulty", "medium");
        String options = (String) payload.getOrDefault("options", "[]");
        String correctAnswer = (String) payload.get("correctAnswer");
        String myAnswer = (String) payload.get("myAnswer");
        String analysis = (String) payload.getOrDefault("analysis", "");

        if (stem == null || stem.isBlank()) {
            return R.fail("题干不能为空");
        }
        if (correctAnswer == null || correctAnswer.isBlank()) {
            return R.fail("正确答案不能为空");
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
    public R<String> removeMistake(@PathVariable Long questionId) {
        Long userId = 1L; // TODO: 从登录信息获取
        mistakeService.removeMistake(userId, questionId);
        return R.ok("删除成功");
    }
}
