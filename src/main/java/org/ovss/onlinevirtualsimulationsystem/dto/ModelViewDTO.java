package org.ovss.onlinevirtualsimulationsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelViewDTO {
    private String modelName;
    private String fileAddress;
    private String uploaderName;
    private LocalDateTime uploadTime;
}

