package com.berliz.JWT;

import com.berliz.models.Notification;
import com.berliz.models.User;
import com.berliz.repositories.NotificationRepo;
import com.berliz.repositories.UserRepo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    UserRepo userRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    NotificationRepo notificationRepo;

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

        if (isWebSocketRequest(httpServletRequest)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }

        if (httpServletRequest.getServletPath().matches("/user/login|/user/signup|" +
                "/user/forgotPassword|/newsletter/add|/newsletter/updateStatus|" +
                "/user/validatePasswordToken|/user/resetPassword|/user/activateAccount|" +
                "/category/getActiveCategories|/contactUs/add|/dashboard/berliz|/trainer/getActiveTrainers|" +
                "/center/getActiveCenters|/user/refreshToken|/ws/.*|/user/quickAdd|/user/sendActivationToken/.*")) {

            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } else {
            // Handle authenticated paths
            String authorizationHeader = httpServletRequest.getHeader("Authorization");
            String token = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
                username = jwtUtility.extractUsername(token);
                userId = jwtUtility.extractUserId(token);
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

    private boolean isWebSocketRequest(HttpServletRequest request) {
        // Check if the "Upgrade" header contains "websocket"
        String upgradeHeader = request.getHeader("Upgrade");
        return "websocket".equalsIgnoreCase(upgradeHeader);
    }

    public String getCurrentUserEmail() {
        if (username != null) {
            return username;
        } else {
            return null;
        }
    }

    public User getCurrentUser() {
        if (username != null) {
            return userRepo.findByEmail(getCurrentUserEmail());
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

    public boolean isAdmin() {
        if (claims != null) {
            return "admin".equalsIgnoreCase((String) claims.get("role"));
        } else {
            return false;
        }
    }

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

    public boolean isMemberClient() {
        return "memberClient".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isMember() {
        return "member".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isStore() {
        return "store".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isDriver() {
        return "driver".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isBerlizUser() {
        String role = (String) claims.get("role");
        if (role != null) {
            String[] validRoles = {"admin", "user", "client", "trainer", "center", "store",
                    "driver", "member", "memberClient"};
            for (String validRole : validRoles) {
                if (validRole.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAccountIncomplete(User user) {
        return user.getFirstname() == null || user.getFirstname().isEmpty() ||
                user.getLastname() == null || user.getLastname().isEmpty() ||
                user.getAddress() == null || user.getAddress().isEmpty() ||
                user.getCountry() == null || user.getCountry().isEmpty() ||
                user.getGender() == null || user.getGender().isEmpty() ||
                user.getCity() == null || user.getCity().isEmpty() ||
                user.getPhone() == null || user.getPhone().isEmpty() ||
                user.getBio() == null || user.getBio().isEmpty() ||
                user.getPostalCode() == null ||
                user.getProfilePhoto() == null;
    }

    public void sendNotifications(String entityEndpoint, String adminNotificationMessage, User user,
                                  String notificationMessage, Object entityOrList) {
        Notification notification = new Notification();
        if (isAdmin()) {
            notification.setNotification(adminNotificationMessage + " by admin: " + getCurrentUserEmail());
        } else {
            notification.setNotification(notificationMessage);
        }

        notification.setUser(user);
        notificationRepo.save(notification);
        if (!isAdmin(user)) {
            List<User> admins = userRepo.findAllAdmins();
            for (User admin : admins) {
                Notification adminNotification = new Notification();
                if (isAdmin()) {
                    adminNotification.setNotification(adminNotificationMessage + " by admin: " + getCurrentUserEmail());
                } else {
                    adminNotification.setNotification(adminNotificationMessage + " by user: " + getCurrentUserEmail());
                }
                adminNotification.setUser(admin);
                notificationRepo.save(adminNotification);
            }
        }

        simpMessagingTemplate.convertAndSend("/topic/notification", notification.getNotification());
        simpMessagingTemplate.convertAndSend(entityEndpoint, entityOrList);
    }

    public boolean isAdmin(User user) {
        return "admin".equalsIgnoreCase((user.getRole()));
    }
}
