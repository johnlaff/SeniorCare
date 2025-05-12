package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.Role;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.security.JwtTokenProvider;
import br.com.uniube.seniorcare.web.dto.request.LoginRequest;
import br.com.uniube.seniorcare.web.dto.request.RefreshTokenRequest;
import br.com.uniube.seniorcare.web.dto.response.TokenResponse;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginShouldReturnTokensWhenCredentialsAreValid() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        String encodedPassword = "encodedPassword";
        String accessToken = "access.token.jwt";
        String refreshToken = "refresh.token.jwt";

        LoginRequest request = new LoginRequest(email, password);
        User user = createUser(email, encodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtTokenProvider.createToken(user)).thenReturn(accessToken);
        when(jwtTokenProvider.createRefreshToken(email)).thenReturn(refreshToken);

        // Act
        ResponseEntity<TokenResponse> response = authController.login(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(accessToken, response.getBody().getAccessToken());
        assertEquals(refreshToken, response.getBody().getRefreshToken());
    }

    @Test
    void loginShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "password";

        LoginRequest request = new LoginRequest(email, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> authController.login(request));
        assertEquals("Credenciais inválidas", exception.getMessage());
    }

    @Test
    void loginShouldThrowExceptionWhenPasswordDoesNotMatch() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongPassword";
        String encodedPassword = "encodedPassword";

        LoginRequest request = new LoginRequest(email, password);
        User user = createUser(email, encodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> authController.login(request));
        assertEquals("Credenciais inválidas", exception.getMessage());
    }

    @Test
    void refreshShouldReturnNewTokensWhenRefreshTokenIsValid() {
        // Arrange
        String email = "test@example.com";
        String refreshToken = "valid.refresh.token";
        String newAccessToken = "new.access.token";
        String newRefreshToken = "new.refresh.token";

        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        User user = createUser(email, "encodedPassword");

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUsername(refreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createToken(user)).thenReturn(newAccessToken);
        when(jwtTokenProvider.createRefreshToken(email)).thenReturn(newRefreshToken);

        // Act
        ResponseEntity<TokenResponse> response = authController.refresh(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newAccessToken, response.getBody().getAccessToken());
        assertEquals(newRefreshToken, response.getBody().getRefreshToken());
    }

    @Test
    void refreshShouldThrowExceptionWhenRefreshTokenIsInvalid() {
        // Arrange
        String refreshToken = "invalid.refresh.token";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> authController.refresh(request));
        assertEquals("Token de atualização inválido ou expirado", exception.getMessage());
    }

    @Test
    void refreshShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        String refreshToken = "valid.refresh.token";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUsername(refreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> authController.refresh(request));
        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void refreshShouldThrowExceptionWhenJwtExceptionOccurs() {
        // Arrange
        String refreshToken = "invalid.refresh.token";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        when(jwtTokenProvider.validateToken(refreshToken)).thenThrow(new JwtException("Invalid token"));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> authController.refresh(request));
        assertEquals("Token de atualização inválido", exception.getMessage());
    }

    private User createUser(String email, String password) {
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPassword(password);
        user.setName("Test User");
        user.setRole(Role.ADMIN);
        user.setOrganization(organization);
        return user;
    }
}