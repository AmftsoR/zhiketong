package com.zhiketong.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhiketong.backend.common.BusinessException;
import com.zhiketong.backend.dto.QuestionDTO;
import com.zhiketong.backend.dto.QuestionImportResultVO;
import com.zhiketong.backend.dto.QuestionQuery;
import com.zhiketong.backend.entity.Question;
import com.zhiketong.backend.listener.QuestionExcelListener;
import com.zhiketong.backend.mapper.KnowledgePointMapper;
import com.zhiketong.backend.mapper.QuestionMapper;
import com.zhiketong.backend.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.IOException;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {

    @Resource
    private KnowledgePointMapper kpMapper;

    @Override
    public Question create(QuestionDTO dto) {
        Question entity = BeanUtil.copyProperties(dto, Question.class);
        save(entity);
        return entity;
    }

    @Override
    public Question update(QuestionDTO dto) {
        Question exist = getById(dto.getId());
        if (exist == null) {
            throw new BusinessException("题目不存在");
        }
        BeanUtil.copyProperties(dto, exist);
        updateById(exist);
        return exist;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        removeById(id);
    }

    @Override
    public Question getById(Long id) {
        return super.getById(id);
    }

    @Override
    public Page<Question> page(QuestionQuery query) {
        Page<Question> page = new Page<>(query.getPage(), query.getSize());
        return baseMapper.selectPageByQuery(page, query);
    }

    @Override
    @Transactional
    public QuestionImportResultVO importExcel(MultipartFile file) {
        QuestionImportResultVO result = new QuestionImportResultVO();
        QuestionExcelListener listener = new QuestionExcelListener(baseMapper, kpMapper, result);
        try {
            EasyExcel.read(file.getInputStream(), QuestionDTO.class, listener).sheet().doRead();
        } catch (IOException e) {
            result.addError(0, "文件读取失败: " + e.getMessage());
        }
        return result;
    }
}
