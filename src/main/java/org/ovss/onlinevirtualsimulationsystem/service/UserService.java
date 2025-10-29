package org.ovss.onlinevirtualsimulationsystem.service;

import org.ovss.onlinevirtualsimulationsystem.dto.UserDTO;
import org.ovss.onlinevirtualsimulationsystem.entity.UserEntity;
import org.ovss.onlinevirtualsimulationsystem.exception.IncorrectPasswordException;
import org.ovss.onlinevirtualsimulationsystem.exception.UserNotFoundException;
import org.ovss.onlinevirtualsimulationsystem.repository.REP_User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private REP_User userRepository;

    public UserDTO login(String userName, String password) {
        UserEntity user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("用户名不存在"));

        if (!user.getPassword().equals(password)) {
            throw new IncorrectPasswordException("密码不正确");
        }

        return new UserDTO(user.getUserId(), user.getUserName(), user.getUserAuthority());
    }
}
