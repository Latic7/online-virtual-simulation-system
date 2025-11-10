package org.ovss.onlinevirtualsimulationsystem.repository;

import org.ovss.onlinevirtualsimulationsystem.entity.ModelEntity;
import org.ovss.onlinevirtualsimulationsystem.enumeration.AuditStatusEnum;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<ModelEntity, Long> {
    @Query("SELECT DISTINCT m FROM ModelEntity m " +
            "LEFT JOIN m.uploader u " +
            "LEFT JOIN m.tags mt " +
            "LEFT JOIN mt.tag t " +
            "WHERE m.isLive = true AND m.auditStatus = 'APPROVED' AND (" +
            "LOWER(m.modelName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.tagName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ModelEntity> searchModels(@Param("search") String search, Sort sort);

    @Query("SELECT DISTINCT m FROM ModelEntity m " +
            "LEFT JOIN m.uploader u " +
            "LEFT JOIN m.tags mt " +
            "LEFT JOIN mt.tag t " +
            "WHERE (:status IS NULL OR m.auditStatus = :status) AND (" +
            "LOWER(m.modelName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.tagName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ModelEntity> searchAllModelsForAdmin(@Param("search") String search, @Param("status") AuditStatusEnum status, Sort sort);

    @Query("SELECT m FROM ModelEntity m WHERE m.isLive = true AND m.auditStatus = 'APPROVED'")
    List<ModelEntity> findAll(Sort sort);

    @Query("SELECT m FROM ModelEntity m WHERE m.modelId = :modelId AND m.isLive = true AND m.auditStatus = 'APPROVED'")
    Optional<ModelEntity> findByIdAndIsLiveTrueAndAuditStatusApproved(@Param("modelId") Long modelId);

    List<ModelEntity> findByAuditStatus(AuditStatusEnum status, Sort sort);

    List<ModelEntity> findByUploader_UserId(Long userId, Sort sort);

    @Query("SELECT m FROM ModelEntity m WHERE m.uploader.userId = :userId AND (:status IS NULL OR m.auditStatus = :status)")
    List<ModelEntity> findByUploader_UserIdAndAuditStatus(@Param("userId") Long userId, @Param("status") AuditStatusEnum status, Sort sort);

    @Query("SELECT DISTINCT m FROM ModelEntity m " +
            "LEFT JOIN m.tags mt " +
            "LEFT JOIN mt.tag t " +
            "WHERE m.uploader.userId = :userId " +
            "AND (:status IS NULL OR m.auditStatus = :status) " +
            "AND (LOWER(m.modelName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.tagName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ModelEntity> searchMyModels(@Param("userId") Long userId, @Param("search") String search, @Param("status") AuditStatusEnum status, Sort sort);

    boolean existsByUploaderAndModelName(org.ovss.onlinevirtualsimulationsystem.entity.UserEntity uploader, String modelName);
}
