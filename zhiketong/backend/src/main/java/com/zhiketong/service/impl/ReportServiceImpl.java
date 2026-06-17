package com.zhiketong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiketong.dto.KnowledgeMasteryDto;
import com.zhiketong.dto.ReportDto;
import com.zhiketong.entity.ShareReport;
import com.zhiketong.mapper.ShareReportMapper;
import com.zhiketong.service.KnowledgeService;
import com.zhiketong.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private ShareReportMapper shareReportMapper;

    /** 雷达图维度标签固定位置 */
    private static final List<ReportDto.RadarLabel> RADAR_LABELS = Arrays.asList(
            createLabel("思维", "50%", "3px"),
            createLabel("运算", "95%", "50%"),
            createLabel("应用", "50%", "92%"),
            createLabel("表达", "4%", "50%")
    );

    @Override
    public ReportDto getMyAnalysis(Long studentId, String period) {
        List<KnowledgeMasteryDto> masteryList = knowledgeService.getStudentMastery(studentId);

        ReportDto report = new ReportDto();

        // 取前3-4个知识点作为章节掌握度
        List<ReportDto.ChapterMastery> chapters = masteryList.stream()
                .limit(4)
                .map(m -> {
                    ReportDto.ChapterMastery ch = new ReportDto.ChapterMastery();
                    ch.setName(m.getKnowledgeName());
                    ch.setValue((int) Math.round(m.getCorrectRate() * 100));
                    ch.setColor(mapColor(m.getColor()));
                    return ch;
                })
                .collect(Collectors.toList());
        report.setChapters(chapters);

        // 雷达数据：取前4个知识点的掌握度
        List<Integer> mineRadar = new ArrayList<>();
        for (int i = 0; i < Math.min(4, masteryList.size()); i++) {
            mineRadar.add((int) Math.round(masteryList.get(i).getCorrectRate() * 100));
        }
        // 补齐到4个维度
        while (mineRadar.size() < 4) {
            mineRadar.add(50);
        }

        // 班级平均（模拟数据，后续可从班级统计接口获取）
        List<Integer> classAvgRadar = Arrays.asList(65, 58, 60, 62);

        ReportDto.RadarData radar = new ReportDto.RadarData();
        radar.setMine(mineRadar);
        radar.setClassAvg(classAvgRadar);
        report.setRadar(radar);
        report.setRadarLabels(RADAR_LABELS);

        // 综合评分：掌握度平均值 * 100
        double avgRate = masteryList.stream()
                .mapToDouble(KnowledgeMasteryDto::getCorrectRate)
                .average()
                .orElse(0.0);
        report.setOverallScore((int) Math.round(avgRate * 100));

        // 摘要
        long masteredCount = masteryList.stream().filter(m -> m.getCorrectRate() >= 0.8).count();
        report.setSummary(String.format(
                "已掌握 %d/%d 个知识点，继续保持[错题回炉 + 靶向练习]的节奏。",
                masteredCount, masteryList.size()));

        return report;
    }

    @Override
    public String shareReport(Long studentId, String reportJson) {
        String token = UUID.randomUUID().toString().replace("-", "");

        ShareReport share = new ShareReport();
        share.setToken(token);
        share.setUserId(studentId);
        share.setReportData(reportJson);
        share.setCreatedAt(LocalDateTime.now());
        share.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7天有效

        shareReportMapper.insert(share);
        return token;
    }

    @Override
    public String getSharedReport(String token) {
        LambdaQueryWrapper<ShareReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareReport::getToken, token);
        ShareReport share = shareReportMapper.selectOne(wrapper);

        if (share == null) return null;

        // 检查是否过期
        if (share.getExpiresAt() != null && share.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        return share.getReportData();
    }

    private static ReportDto.RadarLabel createLabel(String text, String left, String top) {
        ReportDto.RadarLabel label = new ReportDto.RadarLabel();
        label.setText(text);
        label.setLeft(left);
        label.setTop(top);
        return label;
    }

    private static String mapColor(String color) {
        switch (color) {
            case "green": return "#00B894";
            case "yellow": return "#FDCB6E";
            case "red": return "#FF7675";
            default: return "#6C5CE7";
        }
    }
}
