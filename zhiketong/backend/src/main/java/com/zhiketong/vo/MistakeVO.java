package com.zhiketong.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 错题本视图对象 — 聚合了错题记录、题目详情、答题记录
 */
@Data
public class MistakeVO {

    private Long id;                // 错题记录 ID
    private Long questionId;        // 题目 ID
    private String subject;         // 科目（从 knowledge_point 取，暂无则 null）
    private String type;            // 题型
    private String difficulty;      // 难度
    private String stem;            // 题干
    private String options;         // 选项 JSON
    private String myAnswer;        // 我的答案
    private String correctAnswer;   // 正确答案
    private String analysis;        // 解析
    private String knowledgePoint;  // 知识点名称
    private Integer wrongCount;     // 累计做错次数
    private LocalDateTime addedAt;  // 加入错题本时间
}
