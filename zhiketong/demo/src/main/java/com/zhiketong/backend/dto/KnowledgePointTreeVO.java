package com.zhiketong.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgePointTreeVO {
    private Long id;
    private Long parentId;
    private String name;
    private String subject;
    private List<KnowledgePointTreeVO> children;
}
