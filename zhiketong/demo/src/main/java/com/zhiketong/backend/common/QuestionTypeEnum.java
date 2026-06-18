package com.zhiketong.backend.common;

import lombok.Getter;

@Getter
public enum QuestionTypeEnum {
    SINGLE_CHOICE("single", "单选题"),
    MULTI_CHOICE("multi", "多选题"),
    TRUE_FALSE("true_false", "判断题"),
    FILL_BLANK("fill_blank", "填空题"),
    SHORT_ANSWER("short_answer", "简答题");

    private final String code;
    private final String desc;

    QuestionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValid(String code) {
        for (QuestionTypeEnum t : values()) {
            if (t.code.equals(code)) return true;
        }
        return false;
    }
}
