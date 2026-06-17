package com.zhiketong.dto;

import lombok.Data;
import java.util.List;

/**
 * 学情报告 DTO
 */
@Data
public class ReportDto {
    private Integer overallScore;
    private String summary;
    private List<RadarLabel> radarLabels;
    private RadarData radar;
    private List<ChapterMastery> chapters;

    @Data
    public static class RadarLabel {
        private String text;
        private String left;
        private String top;
    }

    @Data
    public static class RadarData {
        private List<Integer> mine;
        private List<Integer> classAvg;
    }

    @Data
    public static class ChapterMastery {
        private String name;
        private Integer value;
        private String color;
    }
}
