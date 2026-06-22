package com.zhiketong.backend.common;

import lombok.Getter;

@Getter
public enum DifficultyEnum {
    EASY("easy", "简单", 1.0),
    MEDIUM("medium", "中等", 2.0),
    HARD("hard", "困难", 3.0);

    private final String code;
    private final String desc;
    private final double weight;

    DifficultyEnum(String code, String desc, double weight) {
        this.code = code;
        this.desc = desc;
        this.weight = weight;
    }

    public static double getWeight(String code) {
        for (DifficultyEnum d : values()) {
            if (d.code.equals(code)) return d.weight;
        }
        return 1.0;
    }

    public static boolean isValid(String code) {
        for (DifficultyEnum d : values()) {
            if (d.code.equals(code)) return true;
        }
        return false;
    }
}
