package com.zhiketong.service;

import com.zhiketong.dto.ReportDto;

/**
 * 学情报告服务接口
 */
public interface ReportService {

    /**
     * 获取学生的学情报告
     * @param studentId 学生ID
     * @param period 时间周期: current / prev / term
     */
    ReportDto getMyAnalysis(Long studentId, String period);

    /**
     * 分享报告，返回分享token
     * @param studentId 学生ID
     * @param reportJson 报告JSON数据
     * @return 分享token
     */
    String shareReport(Long studentId, String reportJson);

    /**
     * 根据token获取分享的报告数据
     * @param token 分享token
     * @return 报告JSON数据，过期或不存在返回null
     */
    String getSharedReport(String token);
}
