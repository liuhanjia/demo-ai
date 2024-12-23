package com.yeebotech.yeeboai.document.controller;

import com.yeebotech.yeeboai.common.dto.ApiResult;
import com.yeebotech.yeeboai.common.oss.FileUploadResponse;
import com.yeebotech.yeeboai.common.oss.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Tag(name = "文件上传", description = "阿里云 OSS 文件上传接口")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private OssService ossService;

    @PostMapping("/upload")
    @Operation(
            summary = "上传文件到 OSS",
            description = "上传文件至阿里云 OSS 存储桶，返回文件访问 URL。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "文件上传成功",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = FileUploadResponse.class))),
                    @ApiResponse(responseCode = "400", description = "请求参数错误"),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误")
            }
    )
    public ResponseEntity<ApiResult<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "filename", required = false) String filename) {
        logger.info("接收到上传请求，文件名: {}, 原始文件名: {}", filename, file.getOriginalFilename());

        // 检查文件是否为空
        if (file.isEmpty()) {
            logger.warn("上传文件为空: {}", filename);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResult.error(400, "上传文件不能为空"));
        }

        // 默认文件名为上传的原始文件名
        if (filename == null || filename.isEmpty()) {
            filename = file.getOriginalFilename();
        }

        try {
            // 调用服务类上传文件
            ossService.uploadFile(filename, file.getInputStream());
            FileUploadResponse response = new FileUploadResponse(filename, "https://shunwei-oms.oss-cn-shenzhen.aliyuncs.com/uploads/laravel%E6%96%87%E6%A1%A3.pdf");

            return ResponseEntity.ok(ApiResult.success(response, 200, "文件上传成功"));

        } catch (Exception e) {
            logger.error("文件上传失败: 文件名: {}, 错误信息: {}", filename, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.error(500, "文件上传失败"));
        }
    }
}
