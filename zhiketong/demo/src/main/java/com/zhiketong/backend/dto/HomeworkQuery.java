package com.zhiketong.backend.dto;

import lombok.Data;

@Data
public class HomeworkQuery {
    private Long teacherId;
    private String className;
    private String difficulty;
    private Integer page = 1;
    private Integer size = 10;
}
