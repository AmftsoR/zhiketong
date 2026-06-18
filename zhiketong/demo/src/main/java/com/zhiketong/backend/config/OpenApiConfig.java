package com.zhiketong.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "智课通后端接口文档",
                version = "1.0.0",
                description = "包含知识点管理、题库管理、练习推送、提交判题、AI教研报告、分层作业发布接口"
        )
)
public class OpenApiConfig {
}
