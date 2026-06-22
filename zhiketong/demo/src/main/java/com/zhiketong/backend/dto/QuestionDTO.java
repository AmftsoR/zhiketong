package com.zhiketong.backend.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class QuestionDTO {
    @ExcelIgnore
    private Long id;
    @ExcelIgnore
    private Long knowledgePointId;
    @ExcelProperty("知识点")
    private String knowledgePointName;
    @ExcelProperty("题型")
    private String type;
    @ExcelProperty("难度")
    private String difficulty;
    @ExcelProperty("题干")
    private String title;
    @ExcelProperty("选项(JSON)")
    private String options;
    @ExcelProperty("正确答案")
    private String answer;
    @ExcelProperty("解析")
    private String explanation;
}
