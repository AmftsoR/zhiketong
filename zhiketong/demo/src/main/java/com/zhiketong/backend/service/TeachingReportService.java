package com.zhiketong.backend.service;

import com.zhiketong.backend.dto.TeachingReportRequestDTO;
import com.zhiketong.backend.dto.TeachingReportVO;

public interface TeachingReportService {

    TeachingReportVO generate(TeachingReportRequestDTO dto);
}
