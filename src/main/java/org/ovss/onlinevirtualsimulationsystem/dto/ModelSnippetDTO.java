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
    private String thumbnailAddress;
    private String modelUrl;
}
