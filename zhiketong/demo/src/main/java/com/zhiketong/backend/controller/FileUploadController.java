package com.zhiketong.backend.controller;

import com.zhiketong.backend.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件为空");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) return Result.error("只支持图片");

        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Path dir = Paths.get(UPLOAD_DIR, dateDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            String orig = file.getOriginalFilename();
            String ext = ".png";
            if (orig != null && orig.contains(".")) ext = orig.substring(orig.lastIndexOf('.'));
            String name = UUID.randomUUID().toString().replace("-", "") + ext;
            file.transferTo(dir.resolve(name).toFile());
            return Result.success(Map.of("url", "/uploads/" + dateDir + "/" + name));
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/base64")
    public Result<Map<String, String>> uploadBase64(@RequestBody Map<String, String> body) {
        String b64 = body.get("base64");
        if (b64 == null || b64.isBlank()) return Result.error("数据为空");

        try {
            String[] parts = b64.split(",");
            String header = parts[0], data = parts.length > 1 ? parts[1] : parts[0];
            String ext = ".png";
            if (header.contains("jpeg") || header.contains("jpg")) ext = ".jpg";
            else if (header.contains("gif")) ext = ".gif";
            else if (header.contains("webp")) ext = ".webp";

            byte[] bytes = Base64.getDecoder().decode(data);
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Path dir = Paths.get(UPLOAD_DIR, dateDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            String name = UUID.randomUUID().toString().replace("-", "") + ext;
            Files.write(dir.resolve(name), bytes);
            return Result.success(Map.of("url", "/uploads/" + dateDir + "/" + name));
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
