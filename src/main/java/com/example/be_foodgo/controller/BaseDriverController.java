package com.example.be_foodgo.controller;

import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseDriverController {

    protected final Logger log;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    protected BaseDriverController(Logger log) {
        this.log = log;
    }

    protected String trichXuatUserIdTuHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtTokenProvider.layUserIdTuToken(token);
    }

    protected ResponseHolder layUserIdHoacTraLoiLoi(HttpServletRequest request) {
        String userId = trichXuatUserIdTuHeader(request);
        if (userId == null) {
            return ResponseHolder.authError(
                    ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
        }
        return ResponseHolder.success(userId);
    }

    public static class ResponseHolder {
        public final String userId;
        public final ApiResponse<?> errorResponse;
        public final boolean isAuthError;

        private ResponseHolder(String userId, ApiResponse<?> errorResponse, boolean isAuthError) {
            this.userId = userId;
            this.errorResponse = errorResponse;
            this.isAuthError = isAuthError;
        }

        public static ResponseHolder success(String userId) {
            return new ResponseHolder(userId, null, false);
        }

        public static ResponseHolder authError(ApiResponse<?> response) {
            return new ResponseHolder(null, response, true);
        }
    }
}
