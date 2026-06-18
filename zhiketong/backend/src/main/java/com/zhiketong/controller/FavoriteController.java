package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /** 添加收藏 */
    @PostMapping
    public R<Map<String, Object>> addFavorite(@RequestBody Map<String, Object> payload,
                                               HttpServletRequest request) {
        Long userId = getUserId(request);
        Long questionId = Long.valueOf(payload.get("questionId").toString());
        boolean success = favoriteService.addFavorite(userId, questionId);
        return success
                ? R.ok(Map.of("message", "收藏成功"))
                : R.fail("已收藏过该题目");
    }

    /** 取消收藏 */
    @DeleteMapping("/{questionId}")
    public R<Map<String, Object>> removeFavorite(@PathVariable Long questionId,
                                                  HttpServletRequest request) {
        Long userId = getUserId(request);
        boolean success = favoriteService.removeFavorite(userId, questionId);
        return success
                ? R.ok(Map.of("message", "已取消收藏"))
                : R.fail("取消收藏失败");
    }

    /** 获取收藏列表 */
    @GetMapping
    public R<List<Map<String, Object>>> getFavorites(HttpServletRequest request) {
        Long userId = getUserId(request);
        return R.ok(favoriteService.getFavorites(userId));
    }

    private Long getUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userId != null ? userId : 1L;
    }
}
