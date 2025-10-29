// ...existing code...
import org.ovss.onlinevirtualsimulationsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class SRV_User implements UserDetailsService {

    @Autowired
    private REP_User userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public String login(String userName, String password) {
        E_User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("用户名不存在"));

        if (!user.getPassword().equals(password)) {
            throw new IncorrectPasswordException("密码不正确");
        }
        DTO_User userDto = new DTO_User(user.getUserId(), user.getUserName(), user.getUserAuthority());
        return jwtUtil.generateToken(userDto);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        E_User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new User(user.getUserName(), user.getPassword(), new ArrayList<>());
    }
}

