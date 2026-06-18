package com.zhiketong.service;

import java.util.List;
import java.util.Map;

public interface FavoriteService {
    /** 添加收藏（已存在则不重复添加） */
    boolean addFavorite(Long userId, Long questionId);

    /** 取消收藏 */
    boolean removeFavorite(Long userId, Long questionId);

    /** 获取用户收藏列表（含题目详情） */
    List<Map<String, Object>> getFavorites(Long userId);
}
