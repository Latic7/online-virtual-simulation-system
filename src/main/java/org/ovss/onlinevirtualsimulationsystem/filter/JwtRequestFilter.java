package org.ovss.onlinevirtualsimulationsystem.filter;

import org.ovss.onlinevirtualsimulationsystem.service.UserService;
import org.ovss.onlinevirtualsimulationsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        System.out.println("\n--- JWT Filter Processing Request: " + request.getRequestURI() + " ---");

        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("1. Authorization Header: " + authorizationHeader);

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("2. JWT found in Authorization header.");
        } else if (request.getCookies() != null) {
            System.out.println("2. No Authorization header, checking cookies: " + Arrays.toString(request.getCookies()));
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt-token".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    System.out.println("   - Found 'jwt-token' cookie.");
                    break;
                }
            }
        } else {
            System.out.println("2. No Authorization header and no cookies found.");
        }

        if (jwt != null) {
            System.out.println("3. JWT is present, attempting to extract username.");
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("   - Successfully extracted username: " + username);
            } catch (Exception e) {
                System.out.println("   - Error extracting username from JWT: " + e.getMessage());
            }
        } else {
            System.out.println("3. JWT is null. Skipping to next filter.");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("4. Username is '" + username + "' and SecurityContext is empty. Validating token...");
            UserDetails userDetails = this.userService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                System.out.println("   - Token is valid. Setting authentication in SecurityContext.");
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } else {
                System.out.println("   - Token is invalid.");
            }
        } else if (username != null) {
            System.out.println("4. SecurityContext already has an authentication for user: " + SecurityContextHolder.getContext().getAuthentication().getName());
        }

        System.out.println("--- Finished JWT Filter ---");
        chain.doFilter(request, response);
    }
}
