package org.ovss.onlinevirtualsimulationsystem.service;

import org.ovss.onlinevirtualsimulationsystem.dto.LoginResponseDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.UserDTO;
import org.ovss.onlinevirtualsimulationsystem.entity.UserEntity;
import org.ovss.onlinevirtualsimulationsystem.exception.IncorrectPasswordException;
import org.ovss.onlinevirtualsimulationsystem.exception.UserNotFoundException;
import org.ovss.onlinevirtualsimulationsystem.repository.UserRepository;
import org.ovss.onlinevirtualsimulationsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponseDTO login(String userName, String password) {
        UserEntity user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("用户名不存在"));

        if (!user.getPassword().equals(password)) {
            throw new IncorrectPasswordException("密码不正确");
        }
        UserDTO userDto = new UserDTO(user.getUserId(), user.getUserName(), user.getUserAuthority());
        String accessToken = jwtUtil.generateToken(userDto);
        String refreshToken = jwtUtil.generateRefreshToken(userDto);

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    public String refreshToken(String refreshToken) {
        if (jwtUtil.validateRefreshToken(refreshToken)) {
            String username = jwtUtil.extractUsername(refreshToken);
            UserEntity user = userRepository.findByUserName(username)
                    .orElseThrow(() -> new UserNotFoundException("用户不存在"));
            UserDTO userDto = new UserDTO(user.getUserId(), user.getUserName(), user.getUserAuthority());
            return jwtUtil.generateToken(userDto);
        } else {
            throw new RuntimeException("Refresh token is expired or invalid");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new User(user.getUserName(), user.getPassword(), new ArrayList<>());
    }
}
