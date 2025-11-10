package org.ovss.onlinevirtualsimulationsystem.controller;

import org.ovss.onlinevirtualsimulationsystem.dto.LoginRequestDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.LoginResponseDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.RegistrationRequestDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.UserDTO;
import org.ovss.onlinevirtualsimulationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequestDTO registrationRequest) {
        userService.register(registrationRequest);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse httpServletResponse) {
        LoginResponseDTO loginResponse = userService.login(loginRequest.getUserName(), loginRequest.getPassword());

        // Using ResponseCookie builder for more options
        ResponseCookie cookie = ResponseCookie.from("jwt-token", loginResponse.getAccessToken())
                .path("/")
                .maxAge(1800) // 30 minutes
                .sameSite("Strict") // Change to Strict for better security
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
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyInfo(Principal principal) {
        UserDTO user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(user);
    }
}
