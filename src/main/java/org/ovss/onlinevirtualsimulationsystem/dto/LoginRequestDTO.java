package org.ovss.onlinevirtualsimulationsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequestDTO {
    private String userName;
    private String password;
}

