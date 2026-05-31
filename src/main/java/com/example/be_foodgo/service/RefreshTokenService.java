package com.example.be_foodgo.service;

import com.example.be_foodgo.model.RefreshToken;
import com.example.be_foodgo.repository.RefreshTokenRepository;
import com.example.be_foodgo.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                                JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshExpirationMs = jwtTokenProvider.getRefreshExpirationMs();
    }

    public RefreshToken taoRefreshToken(String userId, String deviceInfo) throws Exception {
        String tokenValue = jwtTokenProvider.taoRefreshTokenValue(userId);
        long expiresAtMs = System.currentTimeMillis() + refreshExpirationMs;

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .userId(userId)
                .deviceInfo(deviceInfo)
                .createdAt(Instant.now().toString())
                .expiresAt(Instant.ofEpochMilli(expiresAtMs).toString())
                .revoked(false)
                .build();

        String id = refreshTokenRepository.taoRefreshToken(refreshToken);
        refreshToken.setId(id);

        log.info("Tao refresh token moi cho userId: {}, device: {}", userId, deviceInfo);
        return refreshToken;
    }

    public RefreshToken xacThucRefreshToken(String tokenValue) throws Exception {
        if (tokenValue == null || tokenValue.isBlank()) {
            log.warn("Refresh token rong");
            return null;
        }

        if (!jwtTokenProvider.xacThucToken(tokenValue)) {
            log.warn("Refresh token xac thuc that bai");
            return null;
        }

        RefreshToken refreshToken = refreshTokenRepository.timTheoToken(tokenValue);
        if (refreshToken == null) {
            log.warn("Refresh token khong ton tai hoac da bi thu hoi");
            return null;
        }

        if (refreshToken.isRevoked()) {
            log.warn("Refresh token da bi thu hoi");
            return null;
        }

        return refreshToken;
    }

    public void thuHoiRefreshToken(String tokenValue) throws Exception {
        RefreshToken refreshToken = refreshTokenRepository.timTheoToken(tokenValue);
        if (refreshToken != null) {
            refreshTokenRepository.thuHoiToken(refreshToken.getId());
            log.info("Thu hoi refresh token thanh cong - id: {}", refreshToken.getId());
        }
    }

    public void thuHoiTatCaRefreshTokenCuaUser(String userId) throws Exception {
        refreshTokenRepository.thuHoiTatCaTokenCuaUser(userId);
        log.info("Thu hoi tat ca refresh token cua userId: {}", userId);
    }

    public void xoaRefreshToken(String tokenValue) throws Exception {
        RefreshToken refreshToken = refreshTokenRepository.timTheoToken(tokenValue);
        if (refreshToken != null) {
            refreshTokenRepository.xoaToken(refreshToken.getId());
        }
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}
