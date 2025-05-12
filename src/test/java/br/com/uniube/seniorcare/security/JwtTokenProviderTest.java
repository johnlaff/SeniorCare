package br.com.uniube.seniorcare.security;

import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String TEST_SECRET = "testSecretKeyWithAtLeast32CharactersForHS256Algorithm";
    private final long TEST_EXPIRATION = 3600000; // 1 hour
    private final long TEST_REFRESH_EXPIRATION = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "validityInMilliseconds", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshValidityInMilliseconds", TEST_REFRESH_EXPIRATION);
    }

    @Test
    void createTokenFromUser() {
        // Arrange
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setRole(Role.ADMIN);

        // Act
        String token = jwtTokenProvider.createToken(user);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("test@example.com", jwtTokenProvider.getUsername(token));
        assertEquals(user.getId().toString(), jwtTokenProvider.getUserId(token));
    }

    @Test
    void createTokenFromCredentials() {
        // Arrange
        String username = "test@example.com";
        String role = "ADMIN";
        String userId = UUID.randomUUID().toString();

        // Act
        String token = jwtTokenProvider.createToken(username, role, userId);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.getUsername(token));
        assertEquals(userId, jwtTokenProvider.getUserId(token));
    }

    @Test
    void createRefreshToken() {
        // Arrange
        String username = "test@example.com";

        // Act
        String refreshToken = jwtTokenProvider.createRefreshToken(username);

        // Assert
        assertNotNull(refreshToken);
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        assertEquals(username, jwtTokenProvider.getUsername(refreshToken));
    }

    @Test
    void getAuthentication() {
        // Arrange
        String username = "test@example.com";
        String role = "ADMIN";
        String userId = UUID.randomUUID().toString();
        String token = jwtTokenProvider.createToken(username, role, userId);

        // Act
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // Assert
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertTrue(authentication.getPrincipal() instanceof UserDetails);
        assertEquals(username, ((UserDetails) authentication.getPrincipal()).getUsername());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void getClaims() {
        // Arrange
        String username = "test@example.com";
        String role = "ADMIN";
        String userId = UUID.randomUUID().toString();
        String token = jwtTokenProvider.createToken(username, role, userId);

        // Act
        Claims claims = jwtTokenProvider.getClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals(role, claims.get("role", String.class));
        assertEquals(userId, claims.get("userId", String.class));
    }

    @Test
    void validateToken_validToken() {
        // Arrange
        String token = jwtTokenProvider.createToken("test@example.com", "ADMIN", UUID.randomUUID().toString());

        // Act & Assert
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_invalidToken() {
        // Arrange
        String invalidToken = "invalid.token.string";

        // Act & Assert
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }
}