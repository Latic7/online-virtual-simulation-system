package org.ovss.onlinevirtualsimulationsystem.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ovss.onlinevirtualsimulationsystem.dto.UserDTO;
import org.ovss.onlinevirtualsimulationsystem.enumeration.UserAuthorityEnum;
import org.ovss.onlinevirtualsimulationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminRedirectInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            UserDTO user = userService.findByUsername(authentication.getName());
            if (user.getUserAuthority() == UserAuthorityEnum.ADMIN) {
                response.sendRedirect("/admin/dashboard");
                return false; // Stop further processing
            }
        }
        return true; // Continue with the request
    }
}

