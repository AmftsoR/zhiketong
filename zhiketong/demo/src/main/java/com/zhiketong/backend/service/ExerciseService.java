package com.zhiketong.backend.service;

import com.zhiketong.backend.dto.ExercisePushRequestDTO;
import com.zhiketong.backend.dto.ExercisePushVO;
import com.zhiketong.backend.dto.ExerciseSubmitRequestDTO;
import com.zhiketong.backend.dto.ExerciseSubmitVO;

public interface ExerciseService {

    ExercisePushVO push(ExercisePushRequestDTO dto);

    ExerciseSubmitVO submit(ExerciseSubmitRequestDTO dto);
}
