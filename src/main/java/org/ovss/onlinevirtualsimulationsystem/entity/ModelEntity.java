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

    @Column(name = "UploadTime", nullable = false, insertable = false, updatable = false)
    private LocalDateTime uploadTime;

    @Column(name = "AuditStatus", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private AuditStatusEnum auditStatus;

    @Column(name = "Version", nullable = false, insertable = false, updatable = false)
    private Integer version;

    @Column(name = "IsLive", nullable = false, insertable = false, updatable = false)
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
