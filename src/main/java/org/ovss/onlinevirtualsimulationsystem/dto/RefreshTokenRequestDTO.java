package org.ovss.onlinevirtualsimulationsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RefreshTokenRequestDTO {
    private String refreshToken;
}

