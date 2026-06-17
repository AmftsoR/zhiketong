package com.zhiketong.dto;

import lombok.Data;
import java.util.List;

/**
 * 学生学情详情 DTO
 */
@Data
public class StudentDetailDto {
    private Long studentId;
    private String studentName;
    /** 错题列表 */
    private List<WrongRecordDetailDto> wrongList;
    /** 知识点掌握度列表 */
    private List<KnowledgeMasteryDto> masteryList;
}
