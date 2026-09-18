package com.gsir.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersionEntity, UUID> {
    List<FileVersionEntity> findByFileId(UUID fileId);
    
    @Query("SELECT MAX(fv.versionNumber) FROM FileVersionEntity fv WHERE fv.fileId = :fileId")
    Optional<Integer> findMaxVersionByFileId(UUID fileId);
}

