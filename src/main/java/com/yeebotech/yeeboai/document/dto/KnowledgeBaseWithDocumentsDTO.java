package com.yeebotech.yeeboai.document.dto;

import com.yeebotech.yeeboai.document.entity.KnowledgeBase;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class KnowledgeBaseWithDocumentsDTO {

    private Long id;
    private String title;
    private String description;
    private List<DocumentDTO> documents;

    public KnowledgeBaseWithDocumentsDTO(KnowledgeBase knowledgeBase) {
        this.id = knowledgeBase.getId();
        this.title = knowledgeBase.getTitle();
        this.description = knowledgeBase.getDescription();
        this.documents = knowledgeBase.getDocuments().stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }
}

