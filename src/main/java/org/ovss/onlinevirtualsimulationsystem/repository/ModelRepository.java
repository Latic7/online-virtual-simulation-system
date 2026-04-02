package org.ovss.onlinevirtualsimulationsystem.repository;

import org.ovss.onlinevirtualsimulationsystem.entity.ModelEntity;
import org.ovss.onlinevirtualsimulationsystem.enumeration.AuditStatusEnum;
import org.ovss.onlinevirtualsimulationsystem.enumeration.LifecycleStatusEnum;
import org.ovss.onlinevirtualsimulationsystem.enumeration.SubmissionTypeEnum;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<ModelEntity, Long> {
    @Query("SELECT DISTINCT m FROM ModelEntity m " +
            "LEFT JOIN m.uploader u " +
            "LEFT JOIN m.tags mt " +
            "LEFT JOIN mt.tag t " +
            "WHERE m.lifecycleStatus = 'LIVE' AND (" +
            "LOWER(m.modelName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.tagName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ModelEntity> searchModels(@Param("search") String search, Sort sort);

    @Query("SELECT m FROM ModelEntity m WHERE m.lifecycleStatus = 'LIVE'")
    List<ModelEntity> findAll(Sort sort);

    @Query("SELECT DISTINCT m FROM ModelEntity m " +
            "LEFT JOIN m.tags mt " +
            "LEFT JOIN mt.tag t " +
            "WHERE m.uploader.userId = :userId " +
            "AND (:auditStatus IS NULL OR m.auditStatus = :auditStatus) " +
            "AND (:lifecycleStatus IS NULL OR m.lifecycleStatus = :lifecycleStatus) " +
            "AND (:submissionType IS NULL OR m.submissionType = :submissionType) " +
            "AND (:excludedSubmissionType IS NULL OR m.submissionType <> :excludedSubmissionType) " +
            "AND ((:search IS NULL OR :search = '') OR " +
            "LOWER(m.modelName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.tagName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ModelEntity> searchMyModelsByFilter(@Param("userId") Long userId,
                                             @Param("search") String search,
                                             @Param("auditStatus") AuditStatusEnum auditStatus,
                                             @Param("lifecycleStatus") LifecycleStatusEnum lifecycleStatus,
                                             @Param("submissionType") SubmissionTypeEnum submissionType,
                                             @Param("excludedSubmissionType") SubmissionTypeEnum excludedSubmissionType,
                                             Sort sort);

    @Query("SELECT DISTINCT m FROM ModelEntity m " +
            "LEFT JOIN m.uploader u " +
            "LEFT JOIN m.tags mt " +
            "LEFT JOIN mt.tag t " +
            "WHERE (:auditStatus IS NULL OR m.auditStatus = :auditStatus) " +
            "AND (:lifecycleStatus IS NULL OR m.lifecycleStatus = :lifecycleStatus) " +
            "AND (:submissionType IS NULL OR m.submissionType = :submissionType) " +
            "AND (:excludedSubmissionType IS NULL OR m.submissionType <> :excludedSubmissionType) " +
            "AND ((:search IS NULL OR :search = '') OR " +
            "LOWER(m.modelName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.tagName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ModelEntity> searchAllModelsForAdminByFilter(@Param("search") String search,
                                                      @Param("auditStatus") AuditStatusEnum auditStatus,
                                                      @Param("lifecycleStatus") LifecycleStatusEnum lifecycleStatus,
                                                      @Param("submissionType") SubmissionTypeEnum submissionType,
                                                      @Param("excludedSubmissionType") SubmissionTypeEnum excludedSubmissionType,
                                                      Sort sort);

    boolean existsByUploaderAndModelName(org.ovss.onlinevirtualsimulationsystem.entity.UserEntity uploader, String modelName);

        boolean existsByParentModelAndAuditStatusAndLifecycleStatus(ModelEntity parentModel, AuditStatusEnum auditStatus, LifecycleStatusEnum lifecycleStatus);

        long countByUploader_UserId(Long userId);

        long countByUploader_UserIdAndAuditStatus(Long userId, AuditStatusEnum auditStatus);

        long countByUploader_UserIdAndLifecycleStatus(Long userId, LifecycleStatusEnum lifecycleStatus);

        long countByAuditStatus(AuditStatusEnum auditStatus);

        @Query("SELECT COUNT(m) FROM ModelEntity m " +
                        "WHERE m.auditStatus = :auditStatus " +
                        "AND m.lifecycleStatus = :lifecycleStatus " +
                        "AND m.submissionType <> :excludedSubmissionType")
        long countByAuditStatusAndLifecycleStatusAndSubmissionTypeNot(@Param("auditStatus") AuditStatusEnum auditStatus,
                                                                                                                                  @Param("lifecycleStatus") LifecycleStatusEnum lifecycleStatus,
                                                                                                                                  @Param("excludedSubmissionType") SubmissionTypeEnum excludedSubmissionType);

        long countByAuditStatusAndLifecycleStatusAndSubmissionType(AuditStatusEnum auditStatus,
                                                                                                                           LifecycleStatusEnum lifecycleStatus,
                                                                                                                           SubmissionTypeEnum submissionType);
}
