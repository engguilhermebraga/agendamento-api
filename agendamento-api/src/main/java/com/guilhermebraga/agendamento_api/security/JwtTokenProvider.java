package com.guilhermebraga.agendamento_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * Gera e valida os tokens JWT (HS512) da API REST.
 * <p>
 * O segredo vem de {@code jwt.secret}, que no {@code application.properties}
 * aponta para a variável de ambiente {@code JWT_SECRET}:
 * <ul>
 *   <li>informado: precisa ter pelo menos 64 bytes (512 bits, exigência do HS512);</li>
 *   <li>ausente nos perfis {@code dev} ou {@code test} (sem {@code prod} junto): usa uma
 *       chave aleatória gerada a cada inicialização (os tokens deixam de valer no restart);</li>
 *   <li>ausente em qualquer outro perfil: a aplicação não sobe.</li>
 * </ul>
 * A validade do token vem de {@code jwt.expiration-ms} ({@code JWT_EXPIRATION_MS}), entre 1 ms e 30 dias.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    /** HS512 exige uma chave de pelo menos 512 bits. */
    static final int TAMANHO_MINIMO_SEGREDO_BYTES = 64;

    /** Limite da validade do token: 30 dias (também evita overflow no cálculo da data). */
    static final long EXPIRACAO_MAXIMA_MS = Duration.ofDays(30).toMillis();

    /** Chave aleatória só em dev/test, e nunca se o perfil prod também estiver ativo. */
    private static final Profiles PERFIS_CHAVE_ALEATORIA = Profiles.of("(dev | test) & !prod");

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
        @Value("${jwt.secret:}") String secret,
        @Value("${jwt.expiration-ms:86400000}") long expirationMs,
        Environment environment
    ) {
        if (expirationMs <= 0 || expirationMs > EXPIRACAO_MAXIMA_MS) {
            throw new IllegalStateException(
                "jwt.expiration-ms (JWT_EXPIRATION_MS) deve estar entre 1 e " + EXPIRACAO_MAXIMA_MS
                + " ms (30 dias); valor atual: " + expirationMs);
        }
        this.expirationMs = expirationMs;
        this.secretKey = criarChave(secret, environment);
    }

    private static SecretKey criarChave(String secret, Environment environment) {
        if (secret == null || secret.isBlank()) {
            if (environment.acceptsProfiles(PERFIS_CHAVE_ALEATORIA)) {
                log.warn("JWT_SECRET não definido: usando uma chave aleatória (só para dev/test). "
                        + "Os tokens emitidos deixam de valer quando a aplicação reinicia.");
                return Jwts.SIG.HS512.key().build();
            }
            throw new IllegalStateException(
                "JWT_SECRET não definido. Defina a variável de ambiente JWT_SECRET com pelo menos "
                + TAMANHO_MINIMO_SEGREDO_BYTES + " bytes (ex.: openssl rand -base64 64).");
        }

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO_BYTES) {
            throw new IllegalStateException(
                "JWT_SECRET muito curto: são necessários pelo menos " + TAMANHO_MINIMO_SEGREDO_BYTES
                + " bytes para HS512 (recebidos " + bytes.length + ").");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(String usuario) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .subject(usuario)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey, Jwts.SIG.HS512)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsuarioFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Date getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
