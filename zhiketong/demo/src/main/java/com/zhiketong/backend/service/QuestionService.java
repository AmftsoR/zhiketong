package com.zhiketong.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiketong.backend.dto.QuestionDTO;
import com.zhiketong.backend.dto.QuestionImportResultVO;
import com.zhiketong.backend.dto.QuestionQuery;
import com.zhiketong.backend.entity.Question;
import org.springframework.web.multipart.MultipartFile;

public interface QuestionService {

    Question create(QuestionDTO dto);

    Question update(QuestionDTO dto);

    void delete(Long id);

    Question getById(Long id);

    Page<Question> page(QuestionQuery query);

    QuestionImportResultVO importExcel(MultipartFile file);
}
