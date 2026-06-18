package com.zhiketong.backend.dto;

import com.zhiketong.backend.entity.HomeworkTarget;
import com.zhiketong.backend.entity.Question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HomeworkVO {
    private Long id;
    private Long teacherId;
    private String title;
    private String description;
    private List<Long> knowledgePointIds = new ArrayList<>();
    /** 题目详情列表，从题库 question_bank 表关联查询 */
    private List<Question> questions = new ArrayList<>();
    private String difficulty;
    /** 各难度题目实际数量 */
    private int easyCount;
    private int mediumCount;
    private int hardCount;
    private Long classId;
    private String className;
    private String status;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    /** 推送目标 */
    private List<HomeworkTarget> targets = new ArrayList<>();

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Long> getKnowledgePointIds() { return knowledgePointIds; }
    public void setKnowledgePointIds(List<Long> ids) { this.knowledgePointIds = ids; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> qs) { this.questions = qs; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String d) { this.difficulty = d; }
    public int getEasyCount() { return easyCount; }
    public void setEasyCount(int c) { this.easyCount = c; }
    public int getMediumCount() { return mediumCount; }
    public void setMediumCount(int c) { this.mediumCount = c; }
    public int getHardCount() { return hardCount; }
    public void setHardCount(int c) { this.hardCount = c; }
    public Long getClassId() { return classId; }
    public void setClassId(Long id) { this.classId = id; }
    public String getClassName() { return className; }
    public void setClassName(String n) { this.className = n; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime d) { this.deadline = d; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
    public List<HomeworkTarget> getTargets() { return targets; }
    public void setTargets(List<HomeworkTarget> ts) { this.targets = ts; }
}
