package com.zhiketong.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionImportResultVO {
    private int successCount;
    private int failureCount;
    private int totalCount;
    private List<ImportError> errors = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImportError {
        private int rowIndex;
        private String message;
    }

    public void addError(int rowIndex, String message) {
        errors.add(new ImportError(rowIndex, message));
        failureCount++;
    }

    public void addSuccess() {
        successCount++;
    }
}
