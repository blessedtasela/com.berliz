package com.berliz.JWT;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private ClientUserDetailsService clientUserDetailsService;

    Claims claims = null;

    private String username = null;

    private Integer userId = 0;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Match public paths that don't require authentication
        if (httpServletRequest.getServletPath().matches("/user/login|/user/signup|" +
                "/user/forgotPassword|/newsletter/add|/newsletter/updateStatus|" +
                "/user/validatePasswordToken|/user/resetPassword|/user/activateAccount|" +
                "/category/getActiveCategories|/contactUs/add|/dashboard/berliz|/trainer/getActiveTrainers|" +
                "center/getActiveCenters")) {

            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } else {
            // Handle authenticated paths
            String authorizationHeader = httpServletRequest.getHeader("Authorization");
            String token = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
                username = jwtUtility.extractUsername(token);
                userId = Integer.valueOf(jwtUtility.extractUserId(token));
                claims = jwtUtility.extractAllClaims(token);
            }
            if ("OPTIONS".equalsIgnoreCase(httpServletRequest.getMethod())) {
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            } else {
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = clientUserDetailsService.loadUserByUsername(username);

                    if (jwtUtility.isValidToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(httpServletRequest)
                        );

                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }
        }
    }

    // Methods to check user roles based on claims
    public boolean isAdmin() {
        if (claims != null) {
            return "admin".equalsIgnoreCase((String) claims.get("role"));
        } else {
            return false;
        }
    }

    public String getCurrentUser() {
        if (username != null) {
            return username;
        } else {
            return null;
        }
    }

    public Integer getCurrentUserId() {
        if (userId != null) {
            return userId;
        } else {
            return null;
        }
    }

    // Methods to check specific user roles
    public boolean isUser() {
        return "user".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isClient() {
        return "client".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isTrainer() {
        return "trainer".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isCenter() {
        return "center".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isStore() {
        return "store".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isDriver() {
        return "driver".equalsIgnoreCase((String) claims.get("role"));
    }
}
