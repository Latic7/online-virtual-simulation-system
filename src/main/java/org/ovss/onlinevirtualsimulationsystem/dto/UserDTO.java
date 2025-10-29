package org.ovss.onlinevirtualsimulationsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ovss.onlinevirtualsimulationsystem.enumeration.UserAuthorityEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String userName;
    private UserAuthorityEnum userAuthority;
}

