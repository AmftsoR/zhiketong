package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.dto.ReportDto;
import com.zhiketong.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学情报告控制器
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取学生自己的学情报告
     * GET /api/report/my-analysis?period=current
     */
    @GetMapping("/my-analysis")
    public R<ReportDto> myAnalysis(@RequestParam(defaultValue = "current") String period,
                                    HttpServletRequest request) {
        Long userId = resolveUserId(request);
        ReportDto report = reportService.getMyAnalysis(userId, period);
        return R.ok(report);
    }

    /**
     * 分享报告
     * POST /api/report/share
     * 请求体: { reportData: "..." }
     */
    @PostMapping("/share")
    public R<Map<String, String>> shareReport(@RequestBody Map<String, Object> payload,
                                               HttpServletRequest request) {
        Long userId = resolveUserId(request);
        String reportData = (String) payload.getOrDefault("reportData", "{}");
        String token = reportService.shareReport(userId, reportData);
        String shareUrl = "/api/report/shared?token=" + token;
        return R.ok(Map.of("shareToken", token, "shareUrl", shareUrl));
    }

    /**
     * 查看分享的报告（无需登录）
     * GET /api/report/shared?token=xxx
     */
    @GetMapping("/shared")
    public R<String> viewSharedReport(@RequestParam String token) {
        String reportData = reportService.getSharedReport(token);
        if (reportData == null) {
            return R.fail(404, "分享链接不存在或已过期");
        }
        return R.ok(reportData);
    }

    private Long resolveUserId(HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("userId");
        if (loginUserId != null) return loginUserId;
        return 1L;
    }
}
