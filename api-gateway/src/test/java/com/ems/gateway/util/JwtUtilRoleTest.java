package com.ems.gateway.util;

import com.ems.gateway.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilRoleTest {

    private JwtUtil jwtUtil;
    private final String secret = "mySecretKey123456789012345678901234567890";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400L);
    }

    private String createTestToken(String username, String userId, String role, String firstName, String activityStatus) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("firstname", firstName);
        claims.put("role", role);
        claims.put("userId", userId);
        claims.put("activityStatus", activityStatus);
        claims.put("username", username);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void extractRoleFromToken_AdminRole_shouldReturnAdmin() {
        String token = createTestToken("andresclavijor", "1", "1", "Andres", "TRUE");

        JwtUtil.TokenInfo tokenInfo = jwtUtil.parseAndValidateToken(token);

        assertEquals(Role.ADMIN, tokenInfo.getRole());
    }

    @Test
    void extractRoleFromToken_ManagerRole_shouldReturnManager() {
        String token = createTestToken("manager", "2", "2", "Manager", "TRUE");

        JwtUtil.TokenInfo tokenInfo = jwtUtil.parseAndValidateToken(token);

        assertEquals(Role.MANAGER, tokenInfo.getRole());
    }

    @Test
    void extractRoleFromToken_EmployeeRole_shouldReturnEmployee() {
        String token = createTestToken("employee", "3", "3", "Employee", "TRUE");
        
        JwtUtil.TokenInfo tokenInfo = jwtUtil.parseAndValidateToken(token);

        assertEquals(Role.EMPLOYEE, tokenInfo.getRole());
    }

    @Test
    void extractUserInfoFromToken_shouldExtractAllFields() {
        String token = createTestToken("andresclavijor", "1", "1", "Andres", "TRUE");
        JwtUtil.TokenInfo tokenInfo = jwtUtil.parseAndValidateToken(token);
        String username = tokenInfo.getUsername();
        String userId = tokenInfo.getUserId();
        Role role = tokenInfo.getRole();
        String firstName = tokenInfo.getFirstName();
        String activityStatus = tokenInfo.getActivityStatus();

        assertEquals("andresclavijor", username);
        assertEquals("1", userId);
        assertEquals(Role.ADMIN, role);
        assertEquals("Andres", firstName);
        assertEquals("TRUE", activityStatus);
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = createTestToken("testuser", "1", "1", "Test", "TRUE");
        JwtUtil.TokenInfo tokenInfo = jwtUtil.parseAndValidateToken(token);
        assertNotNull(tokenInfo);
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        String invalidToken = "invalid-token";
        JwtUtil.TokenInfo tokenInfo = jwtUtil.parseAndValidateToken(invalidToken);
        assertNull(tokenInfo);
    }
}
