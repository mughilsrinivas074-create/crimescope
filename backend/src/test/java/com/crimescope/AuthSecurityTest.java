package com.crimescope;

import com.crimescope.config.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Security & JWT Authentication Tests")
class AuthSecurityTest {

    private final String secret = "Cr1m3Sc0p3A1T4m1lN4duCr1m3Int3ll1g3nc3C0mm4ndC3nt3rS3cur3K3y2026!";
    private final JwtTokenProvider tokenProvider = new JwtTokenProvider(secret, 3600000L);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("BCrypt password hashing and verification")
    void testPasswordHashing() {
        String rawPassword = "Admin@123_Secure!";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("WrongPassword", encoded));
    }

    @Test
    @DisplayName("JWT Token generation and validation")
    void testJwtTokenGenerationAndValidation() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "analyst_officer", null,
                List.of(new SimpleGrantedAuthority("ROLE_ANALYST"), new SimpleGrantedAuthority("ROLE_VIEWER"))
        );

        String token = tokenProvider.generateToken(auth);
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));

        assertEquals("analyst_officer", tokenProvider.getUsernameFromToken(token));
        String roles = tokenProvider.getRolesFromToken(token);
        assertTrue(roles.contains("ROLE_ANALYST"));
        assertTrue(roles.contains("ROLE_VIEWER"));
    }

    @Test
    @DisplayName("Tampered JWT Token is rejected")
    void testTamperedTokenRejected() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        String token = tokenProvider.generateToken(auth);
        String tampered = token.substring(0, token.length() - 5) + "abcde";

        assertFalse(tokenProvider.validateToken(tampered));
    }
}
