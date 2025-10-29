package org.ovss.onlinevirtualsimulationsystem.controller;

import org.ovss.onlinevirtualsimulationsystem.dto.UserDTO;
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
    public ResponseEntity<UserDTO> login(@RequestBody UserDTO loginRequest) {
        UserDTO user = userService.login(loginRequest.getUserName(), loginRequest.getPassword());
        return ResponseEntity.ok(user);
    }
}
