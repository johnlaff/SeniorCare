package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.security.JwtTokenProvider;
import br.com.uniube.seniorcare.web.dto.request.LoginRequest;
import br.com.uniube.seniorcare.web.dto.request.RefreshTokenRequest;
import br.com.uniube.seniorcare.web.dto.response.TokenResponse;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.uniube.seniorcare.web.dto.request.RegisterRequest;
import br.com.uniube.seniorcare.service.AuthService;
import br.com.uniube.seniorcare.security.LoginAttemptService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "API para autenticação de usuários")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário")
    @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        if (loginAttemptService.isBlocked(request.getEmail())) {
            throw new BusinessException("Usuário temporariamente bloqueado por múltiplas tentativas de login. Tente novamente mais tarde.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    loginAttemptService.loginFailed(request.getEmail());
                    return new BusinessException("Credenciais inválidas");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(request.getEmail());
            throw new BusinessException("Credenciais inválidas");
        }

        loginAttemptService.loginSucceeded(request.getEmail());
        String accessToken = jwtTokenProvider.createToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token de acesso")
    @ApiResponse(responseCode = "200", description = "Token renovado com sucesso")
    @ApiResponse(responseCode = "401", description = "Token de atualização inválido")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new BusinessException("Token de atualização inválido ou expirado");
            }

            String username = jwtTokenProvider.getUsername(refreshToken);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

            String accessToken = jwtTokenProvider.createToken(user);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(username);

            return ResponseEntity.ok(new TokenResponse(accessToken, newRefreshToken));
        } catch (JwtException e) {
            throw new BusinessException("Token de atualização inválido");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastrar novo usuário")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já existe")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).build();
    }
}
