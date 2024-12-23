package com.yeebotech.yeeboai.document.repository;

import com.yeebotech.yeeboai.document.entity.Chunk;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, Long> {
    // JpaRepository 自带了 saveAll 和其他常用方法
    void deleteByDocumentId(Long documentId);
}
