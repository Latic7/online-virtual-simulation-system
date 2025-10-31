package org.ovss.onlinevirtualsimulationsystem.controller;

import org.ovss.onlinevirtualsimulationsystem.dto.LoginRequestDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.LoginResponseDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.RefreshTokenRequestDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.RefreshTokenResponseDTO;
import org.ovss.onlinevirtualsimulationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse httpServletResponse) {
        LoginResponseDTO loginResponse = userService.login(loginRequest.getUserName(), loginRequest.getPassword());

        // Using ResponseCookie builder for more options
        ResponseCookie cookie = ResponseCookie.from("jwt-token", loginResponse.getAccessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(1800) // 30 minutes
                .sameSite("Lax") // Set SameSite attribute
                .build();

        httpServletResponse.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse httpServletResponse) {
        // Clear the access token cookie
        ResponseCookie accessTokenCookie = ResponseCookie.from("jwt-token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader("Set-Cookie", accessTokenCookie.toString());

        // Also clear the refresh token cookie, just in case it exists
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh-token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "jwt-token", required = false) String token, @RequestBody(required = false) RefreshTokenRequestDTO request) {
        String refreshToken = token;
        if (refreshToken == null && request != null) {
            refreshToken = request.getRefreshToken();
        }

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token not found");
        }

        String newAccessToken = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(new RefreshTokenResponseDTO(newAccessToken));
    }
}
