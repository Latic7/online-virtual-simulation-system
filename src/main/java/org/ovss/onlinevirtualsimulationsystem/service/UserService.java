package org.ovss.onlinevirtualsimulationsystem.service;

import org.ovss.onlinevirtualsimulationsystem.dto.LoginResponseDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.RegistrationRequestDTO;
import org.ovss.onlinevirtualsimulationsystem.dto.UserDTO;
import org.ovss.onlinevirtualsimulationsystem.entity.UserEntity;
import org.ovss.onlinevirtualsimulationsystem.enumeration.UserAuthorityEnum;
import org.ovss.onlinevirtualsimulationsystem.exception.IncorrectPasswordException;
import org.ovss.onlinevirtualsimulationsystem.exception.UserAlreadyExistsException;
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

    public void register(RegistrationRequestDTO registrationRequest) {
        if (userRepository.findByUserName(registrationRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        UserEntity newUser = new UserEntity();
        newUser.setUserName(registrationRequest.getUsername());
        newUser.setPassword(registrationRequest.getPassword()); // In a real app, hash this password
        newUser.setUserAuthority(UserAuthorityEnum.USER);
        userRepository.save(newUser);
    }

    public LoginResponseDTO login(String userName, String password) {
        UserEntity user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        if (!user.getPassword().equals(password)) {
            throw new IncorrectPasswordException("Wrong password");
        }
        UserDTO userDto = new UserDTO(user.getUserId(), user.getUserName(), user.getUserAuthority());
        String accessToken = jwtUtil.generateToken(userDto);

        return new LoginResponseDTO(accessToken);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new User(user.getUserName(), user.getPassword(), new ArrayList<>());
    }
}
