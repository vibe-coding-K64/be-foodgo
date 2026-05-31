package com.example.be_foodgo.security;

import com.example.be_foodgo.service.FirebaseAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    private static final String FIREBASE_TOKEN_HEADER = "X-Firebase-Token";
    private static final String FIREBASE_PREFIX = "firebase:";

    private final FirebaseAuthService firebaseAuthService;

    public FirebaseAuthenticationFilter(FirebaseAuthService firebaseAuthService) {
        this.firebaseAuthService = firebaseAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String firebaseToken = request.getHeader(FIREBASE_TOKEN_HEADER);

            if (StringUtils.hasText(firebaseToken)) {
                String firebaseUid = firebaseAuthService.layUidTuIdToken(firebaseToken);
                if (firebaseUid != null) {
                    String internalUserId = FIREBASE_PREFIX + firebaseUid;
                    log.info("Xac thuc Firebase thanh cong - Firebase UID: {} - Path: {}", firebaseUid, request.getRequestURI());
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_USER"));
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(internalUserId, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return;
                } else {
                    log.warn("Firebase token khong hop le tu header: {}", FIREBASE_TOKEN_HEADER);
                }
            }
        } catch (Exception e) {
            log.error("Loi khi xac thuc Firebase token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
