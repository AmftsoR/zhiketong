package com.zhiketong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiketong.entity.Favorite;
import com.zhiketong.entity.QuestionBank;
import com.zhiketong.entity.KnowledgePoint;
import com.zhiketong.mapper.FavoriteMapper;
import com.zhiketong.mapper.QuestionBankMapper;
import com.zhiketong.mapper.KnowledgePointMapper;
import com.zhiketong.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private QuestionBankMapper questionBankMapper;
    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Override
    public boolean addFavorite(Long userId, Long questionId) {
        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getQuestionId, questionId);
        if (favoriteMapper.selectCount(wrapper) > 0) {
            return false; // 已收藏，不重复添加
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setQuestionId(questionId);
        fav.setCreatedAt(LocalDateTime.now());
        return favoriteMapper.insert(fav) > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, Long questionId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getQuestionId, questionId);
        return favoriteMapper.delete(wrapper) > 0;
    }

    @Override
    public List<Map<String, Object>> getFavorites(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .orderByDesc(Favorite::getCreatedAt);
        List<Favorite> favorites = favoriteMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite fav : favorites) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", fav.getId());
            item.put("questionId", fav.getQuestionId());
            item.put("createdAt", fav.getCreatedAt());

            QuestionBank question = questionBankMapper.selectById(fav.getQuestionId());
            if (question != null) {
                item.put("type", question.getType());
                item.put("difficulty", question.getDifficulty());
                item.put("stem", question.getTitle());
                item.put("options", question.getOptions());
                item.put("correctAnswer", parseAnswer(question.getAnswer()));
                item.put("analysis", question.getExplanation());
                if (question.getKnowledgePointId() != null) {
                    KnowledgePoint kp = knowledgePointMapper.selectById(question.getKnowledgePointId());
                    if (kp != null) {
                        item.put("subject", kp.getSubject());
                        item.put("knowledgePoint", kp.getName());
                    }
                }
            }
            result.add(item);
        }
        return result;
    }

    private String parseAnswer(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            if (inner.startsWith("\"") && inner.endsWith("\"")) {
                inner = inner.substring(1, inner.length() - 1);
            }
            return inner;
        }
        return trimmed;
    }
}
