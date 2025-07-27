package com.ems.gateway.util;

import com.ems.gateway.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenValid(String token, String username) {
        final JwtUtil.TokenInfo tokenInfo = parseAndValidateToken(token);
        return (tokenInfo != null && tokenInfo.getUsername().equals(username));
    }

    public TokenInfo parseAndValidateToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            
            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                return null; // Token expired
            }
            
            return new TokenInfo(
                claims.get("userId", String.class),
                Role.fromString(claims.get("role", String.class)),
                claims.get("activityStatus", String.class),
                claims.get("firstname", String.class),
                claims.getSubject()
            );
        } catch (SignatureException e) {
            System.err.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("JWT token validation failed: " + e.getMessage());
        }
        return null; // Invalid token
    }

    /**
     * Container class for JWT token information to avoid multiple parsing operations.
     */
    public static class TokenInfo {
        private final String userId;
        private final Role role;
        private final String activityStatus;
        private final String firstName;
        private final String username;

        public TokenInfo(String userId, Role role, String activityStatus, String firstName, String username) {
            this.userId = userId;
            this.role = role;
            this.activityStatus = activityStatus;
            this.firstName = firstName;
            this.username = username;
        }

        public String getUserId() { return userId; }
        public Role getRole() { return role; }
        public String getActivityStatus() { return activityStatus; }
        public String getFirstName() { return firstName; }
        public String getUsername() { return username; }
    }
}
