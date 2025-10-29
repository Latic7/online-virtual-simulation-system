package org.ovss.onlinevirtualsimulationsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ovss.onlinevirtualsimulationsystem.enumeration.ENUM_UserAuthority;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class E_User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "UserName", nullable = false)
    private String userName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "UserAuthority", nullable = false)
    private ENUM_UserAuthority userAuthority;

    @Column(name = "Password", nullable = false)
    private String password;
}
