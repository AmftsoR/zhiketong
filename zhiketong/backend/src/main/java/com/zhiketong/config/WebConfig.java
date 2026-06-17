package com.zhiketong.config;

import com.zhiketong.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — 注册拦截器 + 静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/questions/random",   // 随机出题无需登录
                        "/api/mistakes",            // 错题本 — Controller 有 fallback
                        "/api/mistakes/**",         // 错题本子路径（删除/上传等）
                        "/api/answer/**",           // 答题提交
                        "/api/knowledge/**",        // 知识掌握度图谱
                        "/api/leaderboard",         // 排行榜
                        "/api/class/**",            // 班级相关
                        "/api/teacher/**"           // 教师统计 + 学生详情
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 拍照上传的图片可通过 /uploads/** 访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
