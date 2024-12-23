package com.yeebotech.yeeboai.document.dto;

import com.yeebotech.yeeboai.document.entity.Document;
import lombok.Data;

import java.util.Date;

@Data
public class DocumentDTO {
    private Long id;
    private String fileName;
    private long fileSize;
    private String fileUrl;
    private Date uploadTime;

    public DocumentDTO(Document document) {
        this.id = document.getId();
        this.fileName = document.getFileName();
        this.fileSize = document.getFileSize();
        this.fileUrl = document.getFileUrl();
        this.uploadTime = document.getUploadTime();
    }
}
