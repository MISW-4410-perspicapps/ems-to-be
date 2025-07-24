package com.ems.gateway.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "mySecretKey123456789012345678901234567890";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400L);
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        String invalidToken = "invalid-token";
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void validateToken_withNullToken_shouldReturnFalse() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void validateToken_withEmptyToken_shouldReturnFalse() {
        assertFalse(jwtUtil.validateToken(""));
    }
}
