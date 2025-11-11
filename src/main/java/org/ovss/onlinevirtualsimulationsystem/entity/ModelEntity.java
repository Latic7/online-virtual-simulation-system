package org.ovss.onlinevirtualsimulationsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ovss.onlinevirtualsimulationsystem.enumeration.AuditStatusEnum;

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

    // 版本号由数据库默认值提供，暂不在应用层更新
    @Column(name = "Version", nullable = false, insertable = false, updatable = false)
    private Integer version;

    // 允许应用层更新 IsLive（上线/下线）
    @Column(name = "IsLive", nullable = false)
    private Boolean isLive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentModelID", insertable = false, updatable = false)
    private ModelEntity parentModel;

    @OneToMany(mappedBy = "model")
    private Set<ModelTagEntity> tags;

    public void setParentModel(ModelEntity parentModel) {
        this.parentModel = parentModel;
    }

    public void setIsLive(boolean isLive) {
        this.isLive = isLive;
    }
}
