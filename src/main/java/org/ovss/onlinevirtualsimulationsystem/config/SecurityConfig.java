package org.ovss.onlinevirtualsimulationsystem.config;

import org.ovss.onlinevirtualsimulationsystem.enumeration.UserAuthorityEnum;
import org.ovss.onlinevirtualsimulationsystem.filter.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendRedirect("/login?required=true");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/login", "/login", "/home", "/error", "/logout", "/register", "/api/users/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/thumbnails/**", "/assets/**", "/models/**").permitAll()
                        .requestMatchers("/api/public/**", "/model/**").permitAll()
                        .requestMatchers("/profile", "/api/users/me", "/api/models/my-models/**", "/api/models/upload").hasAuthority(UserAuthorityEnum.USER.name()) // User only
                        .requestMatchers("/admin/**", "/api/admin/**").hasAuthority(UserAuthorityEnum.ADMIN.name()) // Admin only
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(e -> e.accessDeniedHandler((request, response, accessDeniedException) -> {
            if (request.isUserInRole(UserAuthorityEnum.ADMIN.name()) && "/home".equals(request.getRequestURI())) {
                response.sendRedirect("/admin/dashboard");
            } else {
                response.sendRedirect("/error/403");
            }
        }));

        return http.build();
    }
}
