package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.security.JwtTokenProvider;
import br.com.uniube.seniorcare.service.AuthService;
import br.com.uniube.seniorcare.security.LoginAttemptService;
import br.com.uniube.seniorcare.web.dto.request.LoginRequest;
import br.com.uniube.seniorcare.web.dto.request.RefreshTokenRequest;
import br.com.uniube.seniorcare.web.dto.request.RegisterRequest;
import br.com.uniube.seniorcare.web.dto.response.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthService authService;
    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_sucesso() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@email.com");
        request.setPassword("senha");
        User user = new User();
        user.setEmail("user@email.com");
        user.setPassword("senhaCriptografada");

        when(loginAttemptService.isBlocked(request.getEmail())).thenReturn(false);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createToken(user)).thenReturn("accessToken");
        when(jwtTokenProvider.createRefreshToken(user.getEmail())).thenReturn("refreshToken");

        ResponseEntity<TokenResponse> response = authController.login(request);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("accessToken", response.getBody().getAccessToken());
        assertEquals("refreshToken", response.getBody().getRefreshToken());
        verify(loginAttemptService).loginSucceeded(request.getEmail());
    }

    @Test
    void login_usuario_bloqueado() {
        LoginRequest request = new LoginRequest();
        request.setEmail("bloqueado@email.com");
        when(loginAttemptService.isBlocked(request.getEmail())).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class, () -> authController.login(request));
        assertTrue(ex.getMessage().contains("bloqueado"));
    }

    @Test
    void login_usuario_nao_encontrado() {
        LoginRequest request = new LoginRequest();
        request.setEmail("naoencontrado@email.com");
        request.setPassword("senha");
        when(loginAttemptService.isBlocked(request.getEmail())).thenReturn(false);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () -> authController.login(request));
        assertTrue(ex.getMessage().contains("Credenciais inválidas"));
        verify(loginAttemptService).loginFailed(request.getEmail());
    }

    @Test
    void login_senha_incorreta() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@email.com");
        request.setPassword("senhaerrada");
        User user = new User();
        user.setEmail("user@email.com");
        user.setPassword("senhaCriptografada");
        when(loginAttemptService.isBlocked(request.getEmail())).thenReturn(false);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);
        BusinessException ex = assertThrows(BusinessException.class, () -> authController.login(request));
        assertTrue(ex.getMessage().contains("Credenciais inválidas"));
        verify(loginAttemptService).loginFailed(request.getEmail());
    }

    @Test
    void refresh_token_valido() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("validRefreshToken");
        User user = new User();
        user.setEmail("user@email.com");
        when(jwtTokenProvider.validateToken("validRefreshToken")).thenReturn(true);
        when(jwtTokenProvider.getUsername("validRefreshToken")).thenReturn("user@email.com");
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createToken(user)).thenReturn("novoAccessToken");
        when(jwtTokenProvider.createRefreshToken("user@email.com")).thenReturn("novoRefreshToken");

        ResponseEntity<TokenResponse> response = authController.refresh(request);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("novoAccessToken", response.getBody().getAccessToken());
        assertEquals("novoRefreshToken", response.getBody().getRefreshToken());
    }

    @Test
    void refresh_token_invalido() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid");
        when(jwtTokenProvider.validateToken("invalid")).thenReturn(false);
        BusinessException ex = assertThrows(BusinessException.class, () -> authController.refresh(request));
        assertTrue(ex.getMessage().contains("Token de atualização inválido"));
    }

    @Test
    void refresh_usuario_nao_encontrado() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid");
        when(jwtTokenProvider.validateToken("valid")).thenReturn(true);
        when(jwtTokenProvider.getUsername("valid")).thenReturn("naoexiste@email.com");
        when(userRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () -> authController.refresh(request));
        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
    }

    @Test
    void register_sucesso() {
        RegisterRequest request = new RegisterRequest();
        doNothing().when(authService).register(any(RegisterRequest.class));
        ResponseEntity<Void> response = authController.register(request);
        assertEquals(201, response.getStatusCode().value());
        verify(authService).register(request);
    }
}

