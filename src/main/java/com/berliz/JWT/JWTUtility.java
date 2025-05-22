package com.berliz.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

@Service
public class JWTUtility {

    @Autowired
    private ClientUserDetailsService clientUserDetailsService;

    @Value("${JWT_SECRET}")
    private String SECRET_KEY;

    private static final DecimalFormat formatter = new DecimalFormat("000000");
    private static final Random random = new Random();

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public Integer extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return Integer.parseInt(claims.getId());
    }

    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private String createAccessToken(Map<String, Object> claims, String username, Integer id) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setId(String.valueOf(id))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
    }

    private String createRefreshToken(Map<String, Object> claims, String username, Integer id) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setId(String.valueOf(id))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
    }

    public String generateAccessToken(String accessToken) {
        Claims claims =  extractAllClaims(accessToken);
        String role = clientUserDetailsService.getUserDetails().getRole();
        String username = clientUserDetailsService.getUserDetails().getEmail();
        Integer id = clientUserDetailsService.getUserDetails().getId();
        claims.put("role", role);
        claims.put("id", id);
        return createAccessToken(claims, username, id);
    }

    public String generateRefreshToken(String username, Integer id) {
        Map<String, Object> claims = new HashMap<>();
        return createRefreshToken(claims, username, id);
    }

    public String generatePasswordResetToken(String subject) {
        long currentTimeMillis = System.currentTimeMillis();
        long expirationMillis = currentTimeMillis + 3600000;

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(expirationMillis))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String generateConfirmAccountToken() {
       return RandomStringUtils.randomNumeric(8);
    }

    public Boolean isValidToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public static String getUniqueRandomNumber() {
        int number = random.nextInt(999999) + 1; // Generates number between 1 and 999999
        return formatter.format(number); // Format number as 000000
    }
}
