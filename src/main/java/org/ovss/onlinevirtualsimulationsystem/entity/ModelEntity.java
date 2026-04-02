package org.ovss.onlinevirtualsimulationsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ovss.onlinevirtualsimulationsystem.enumeration.AuditStatusEnum;
import org.ovss.onlinevirtualsimulationsystem.enumeration.LifecycleStatusEnum;
import org.ovss.onlinevirtualsimulationsystem.enumeration.SubmissionTypeEnum;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "model")
public class ModelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ModelID")
    private Long modelId;

    @Column(name = "ModelName", nullable = false)
    private String modelName;

    @Column(name = "ThumbnailAddress", length = 512)
    private String thumbnailAddress;

    @Column(name = "FileAddress", nullable = false, length = 512)
    private String fileAddress;

    @ManyToOne
    @JoinColumn(name = "Uploader", nullable = false)
    private UserEntity uploader;

    // 由数据库填充与更新
    @Column(name = "UploadTime", nullable = false, insertable = false, updatable = false)
    private LocalDateTime uploadTime;

    // 允许应用层更新审核状态（从 PENDING -> APPROVED/REJECTED）
    @Column(name = "AuditStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditStatusEnum auditStatus;

    @Column(name = "LifecycleStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    private LifecycleStatusEnum lifecycleStatus;

    @Column(name = "SubmissionType", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubmissionTypeEnum submissionType;

    @Column(name = "Version", nullable = false)
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentModelID")
    private ModelEntity parentModel;

    @OneToMany(mappedBy = "model")
    private Set<ModelTagEntity> tags;

    public void setParentModel(ModelEntity parentModel) {
        this.parentModel = parentModel;
    }
}
