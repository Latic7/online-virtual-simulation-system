package org.ovss.onlinevirtualsimulationsystem.controller;

import org.ovss.onlinevirtualsimulationsystem.dto.LoginRequestDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.LoginResponseDTO;
// ... existing code ...
import org.ovss.onlinevirtualsimulationsystem.dto.RefreshTokenRequestDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.RefreshTokenResponseDTO;
import org.ovss.onlinevirtualsimulationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = userService.login(loginRequest.getUserName(), loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDTO request) {
// ... existing code ...

        String newAccessToken = userService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(new RefreshTokenResponseDTO(newAccessToken));
    }
}
