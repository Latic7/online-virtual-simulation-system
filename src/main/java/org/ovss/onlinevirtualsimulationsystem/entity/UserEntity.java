package org.ovss.onlinevirtualsimulationsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ovss.onlinevirtualsimulationsystem.enumeration.UserAuthorityEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "UserName", nullable = false)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "UserAuthority", nullable = false)
    private UserAuthorityEnum userAuthority;

    @Column(name = "Password", nullable = false)
    private String password;
}
