package com.zhiketong.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zhiketong.backend.mapper")
public class AppBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppBackendApplication.class, args);
    }
}
