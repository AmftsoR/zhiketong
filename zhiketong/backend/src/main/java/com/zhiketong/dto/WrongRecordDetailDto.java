package com.zhiketong.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 错题详情 DTO（连表查询结果）
 */
@Data
public class WrongRecordDetailDto {
    private Long wrongId;
    private Long questionId;
    private String questionContent;
    private String correctAnswer;
    private String wrongAnswer;
    private String knowledgeName;
    private LocalDateTime wrongTime;
    private String imageUrl;
}
