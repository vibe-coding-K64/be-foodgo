package com.example.be_foodgo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey secretKey;
    private final long expirationMs;
    private final String bearerPrefix;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs,
            @Value("${jwt.bearer-prefix}") String bearerPrefix) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.bearerPrefix = bearerPrefix;
    }

    public String taoToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        log.info("Dang tao JWT cho userId: {}", userId);
        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String taoTempToken(String userId, long tempExpirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tempExpirationMs);
        log.info("Dang tao JWT tam thoi (reset password) cho userId: {}", userId);
        return Jwts.builder()
                .subject(userId)
                .claim("type", "TEMP_TOKEN")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String layUserIdTuToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String userId = claims.getSubject();
            log.debug("Da lay userId tu token: {}", userId);
            return userId;
        } catch (ExpiredJwtException e) {
            log.warn("Token da het han: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.warn("Token khong hop le: {}", e.getMessage());
            return null;
        }
    }

    public boolean xacThucToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token da het han: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Token khong hop le: {}", e.getMessage());
            return false;
        }
    }

    public String layTokenTuHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(bearerPrefix + " ")) {
            return authHeader.substring(bearerPrefix.length() + 1);
        }
        return null;
    }

    public String getBearerPrefix() {
        return bearerPrefix;
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
