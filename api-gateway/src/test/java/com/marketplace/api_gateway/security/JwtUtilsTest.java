package com.marketplace.api_gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void generateToken_shouldWorkWithInjectedSecret() {
        String token = jwtUtils.generateToken("testuser", 123L);
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
    }
}
