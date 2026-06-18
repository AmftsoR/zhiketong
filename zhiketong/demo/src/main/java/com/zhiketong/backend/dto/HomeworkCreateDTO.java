package com.zhiketong.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HomeworkCreateDTO {
    private Long teacherId;
    private String title;
    private String description;
    /** 知识点ID列表 — 必填，系统据此从题库抽题 */
    private List<Long> knowledgePointIds;
    /** 题目ID列表 — 可选，传了则直接使用；不传则按知识点+难度从题库自动抽题 */
    private List<Long> questionIds;
    /** 作业总体难度 — 支持 mixed 表示混合难度 */
    private String difficulty;
    /** 各难度题目数量（difficulty=mixed时生效） */
    private Integer easyCount;
    private Integer mediumCount;
    private Integer hardCount;
    /** 题目总数（difficulty非mixed时生效） */
    private Integer totalCount;
    /** 推送班级ID — 关联 class 表 */
    private Long classId;
    /** 推送班级名称 — classId存在时可以从此读取，不再手填 */
    private String className;
    /** 指定推送学生ID列表 */
    private List<Long> studentIds;
    /** 截止时间 */
    private LocalDateTime deadline;
}
