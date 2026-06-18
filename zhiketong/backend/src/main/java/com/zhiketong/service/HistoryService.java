package com.zhiketong.service;

import java.util.Map;

public interface HistoryService {
    /** 获取学习历史记录 */
    Map<String, Object> getHistory(Long userId, String subject, String startDate, String endDate);
}
