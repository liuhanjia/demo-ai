package com.yeebotech.yeeboai.common.oss;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class OssService {

    private MinioClient ossClient;

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.accessKey}")
    private String accessKey;

    @Value("${oss.accessSecret}")
    private String accessSecret;

    @Value("${oss.bucketName}")
    private String bucketName;

    @PostConstruct
    public void init() {
        this.ossClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, accessSecret)
                .build();
    }

    // 上传文件
    public void uploadFile(String filename, InputStream fileInputStream) throws Exception {
        ossClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(fileInputStream, -1, 10485760) // -1 表示未知的流大小，10485760 是 part 大小限制（10MB）
                        .build());

        fileInputStream.close();
    }

    // 下载文件
    public String downloadFile(String filename) throws Exception {
        String url = ossClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(filename)
                        .expiry(60 * 5) // 5分钟有效期
                        .build());
        System.out.println(url);
        return url;
    }

    // 删除文件
    public void deleteFile(String filename) throws Exception {
        try {
            ossClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build());
            System.out.println("Successfully deleted file: " + filename);
        } catch (Exception e) {
            System.err.println("Error deleting file: " + filename);
            throw e; // 抛出异常供上层捕获或处理
        }
    }
}
