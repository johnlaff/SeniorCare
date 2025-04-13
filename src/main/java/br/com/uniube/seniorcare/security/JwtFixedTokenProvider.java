package br.com.uniube.seniorcare.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtFixedTokenProvider {

    private static final String SECRET_KEY = "chaveSecretaTemporariaParaTestesDoSeniorCareApi123456789";
    private final String fixedToken;

    public JwtFixedTokenProvider() {
        this.fixedToken = gerarTokenFixo();
        // Exibe o token no console na inicialização
        System.out.println("\n\n===== TOKEN JWT FIXO PARA TESTES =====\n" + fixedToken + "\n=====================================\n");
    }

    private String gerarTokenFixo() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject("admin@exemplo.com")
                .claim("roles", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 31536000000L)) // 1 ano
                .signWith(key)
                .compact();
    }

    public String getFixedToken() {
        return fixedToken;
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}