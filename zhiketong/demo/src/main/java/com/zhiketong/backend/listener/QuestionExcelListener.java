package com.zhiketong.backend.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.zhiketong.backend.dto.QuestionDTO;
import com.zhiketong.backend.dto.QuestionImportResultVO;
import com.zhiketong.backend.entity.Question;
import com.zhiketong.backend.entity.KnowledgePoint;
import com.zhiketong.backend.mapper.KnowledgePointMapper;
import com.zhiketong.backend.mapper.QuestionMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class QuestionExcelListener implements ReadListener<QuestionDTO> {

    private static final Map<String, String> TYPE_MAP = Map.of(
            "单选题", "single",
            "多选题", "multi",
            "判断题", "true_false",
            "填空题", "fill_blank",
            "简答题", "short_answer"
    );

    private static final Map<String, String> DIFFICULTY_MAP = Map.of(
            "简单", "easy",
            "中等", "medium",
            "困难", "hard"
    );

    private final QuestionMapper questionMapper;
    private final KnowledgePointMapper kpMapper;
    private final QuestionImportResultVO result;
    private int rowIndex = 0;

    public QuestionExcelListener(QuestionMapper questionMapper, KnowledgePointMapper kpMapper,
                                 QuestionImportResultVO result) {
        this.questionMapper = questionMapper;
        this.kpMapper = kpMapper;
        this.result = result;
    }

    @Override
    public void invoke(QuestionDTO dto, AnalysisContext context) {
        rowIndex++;
        try {
            // 知识点名称 → ID
            Long kpId = dto.getKnowledgePointId();
            String kpName = dto.getKnowledgePointName();
            if (kpId == null && kpName != null && !kpName.trim().isEmpty()) {
                KnowledgePoint kp = kpMapper.selectByName(kpName.trim());
                if (kp != null) {
                    kpId = kp.getId();
                } else {
                    result.addError(rowIndex, "知识点不存在：" + kpName + "，请先在知识点管理中创建");
                    return;
                }
            }

            String type = dto.getType() != null ? dto.getType().trim() : "";
            String difficulty = dto.getDifficulty() != null ? dto.getDifficulty().trim() : "";

            // 中文 → 英文代码转换（已为英文代码时保留原值）
            String typeCode = TYPE_MAP.getOrDefault(type, type);
            String diffCode = DIFFICULTY_MAP.getOrDefault(difficulty, difficulty);

            Question entity = new Question();
            entity.setKnowledgePointId(kpId);
            entity.setType(typeCode);
            entity.setDifficulty(diffCode);
            entity.setTitle(dto.getTitle());
            entity.setOptions(dto.getOptions());
            entity.setAnswer(dto.getAnswer());
            entity.setExplanation(dto.getExplanation());
            questionMapper.insert(entity);
            result.addSuccess();
        } catch (Exception e) {
            result.addError(rowIndex, e.getMessage());
            log.warn("Excel row {} import failed: {}", rowIndex, e.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        result.setTotalCount(result.getSuccessCount() + result.getFailureCount());
        log.info("Excel import done: success={}, failure={}", result.getSuccessCount(), result.getFailureCount());
    }
}
