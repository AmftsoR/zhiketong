package com.zhiketong.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiketong.backend.dto.HomeworkCreateDTO;
import com.zhiketong.backend.dto.HomeworkQuery;
import com.zhiketong.backend.dto.HomeworkVO;
import java.util.List;
import com.zhiketong.backend.entity.Homework;

public interface HomeworkService {

    HomeworkVO create(HomeworkCreateDTO dto);

    HomeworkVO getDetail(Long id);

    Page<Homework> page(HomeworkQuery query);

    void delete(Long id);

    HomeworkVO publish(Long id);

    List<HomeworkVO> getByClassName(String className);
}
