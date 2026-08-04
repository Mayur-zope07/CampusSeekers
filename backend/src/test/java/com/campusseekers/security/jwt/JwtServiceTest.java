package com.campusseekers.security.jwt;

import com.campusseekers.entity.Role;
import com.campusseekers.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Inject properties programmatically for unit testing isolation
        ReflectionTestUtils.setField(jwtService, "secretKey", "9a4f2c8d3e1b7f6a5c4d3e2b1a0f9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a21");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 1 day
    }

    @Test
    void shouldGenerateAndValidateToken() {
        User user = User.builder()
                .email("student@example.com")
                .role(Role.STUDENT)
                .build();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("student@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void shouldGenerateTokenWithClaims() {
        User user = User.builder()
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", Role.ADMIN.name());
        extraClaims.put("customKey", "customVal");

        String token = jwtService.generateToken(extraClaims, user);

        assertNotNull(token);
        assertEquals("admin@example.com", jwtService.extractUsername(token));
        assertEquals(Role.ADMIN.name(), jwtService.extractClaim(token, claims -> claims.get("role", String.class)));
        assertEquals("customVal", jwtService.extractClaim(token, claims -> claims.get("customKey", String.class)));
    }
}
