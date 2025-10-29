package org.ovss.onlinevirtualsimulationsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ovss.onlinevirtualsimulationsystem.enumeration.AuditStatusEnum;

import java.time.LocalDateTime;

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

    @Column(name = "UploadTime", nullable = false)
    private LocalDateTime uploadTime;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "AuditStatus", nullable = false)
    private AuditStatusEnum auditStatus;
}
