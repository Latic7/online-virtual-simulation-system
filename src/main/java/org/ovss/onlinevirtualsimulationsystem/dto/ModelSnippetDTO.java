package org.ovss.onlinevirtualsimulationsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelSnippetDTO {
    private Long modelId;
    private String modelName;
    private List<String> tags;
    private LocalDateTime uploadTime;
    private String uploaderName;
    private String thumbnailUrl;
    private String fileAddress;
    private String auditStatus;

    public ModelSnippetDTO(Long modelId, String modelName, List<String> tags, LocalDateTime uploadTime, String uploaderName, String thumbnailUrl, String fileAddress) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.tags = tags;
        this.uploadTime = uploadTime;
        this.uploaderName = uploaderName;
        this.thumbnailUrl = thumbnailUrl;
        this.fileAddress = fileAddress;
    }
}
