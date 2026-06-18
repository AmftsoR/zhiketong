package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.service.HistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    /** 获取学习历史记录列表 */
    @GetMapping("/list")
    public R<Map<String, Object>> getHistory(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) userId = 1L;
        return R.ok(historyService.getHistory(userId, subject, startDate, endDate));
    }
}
