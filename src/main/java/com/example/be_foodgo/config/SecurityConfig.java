package com.example.be_foodgo.config;

import com.example.be_foodgo.security.FirebaseAuthenticationFilter;
import com.example.be_foodgo.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FirebaseAuthenticationFilter firebaseAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          FirebaseAuthenticationFilter firebaseAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.firebaseAuthenticationFilter = firebaseAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"success\":false,\"message\":\"Token het han hoac khong hop le. Vui long dang nhap lai.\",\"code\":401,\"data\":null}");
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/register/verify-email",
                                "/api/auth/register/complete",
                                "/api/auth/register-driver/send-otp",
                                "/api/auth/register-driver/complete",
                                "/api/auth/login",
                                "/api/auth/send-otp",
                                "/api/auth/verify-otp",
                                "/api/auth/reset-password",
                                "/api/auth/register-merchant",
                                "/api/auth/check-merchant",
                                "/api/auth/refresh-token",
                                "/api/auth/logout",
                                "/api/auth/firebase/link",
                                "/api/auth/send-verify-email-otp",
                                "/api/auth/verify-email"
                        ).permitAll()
                        .requestMatchers(
                                "/api/stores/{id}",
                                "/api/stores/nearby",
                                "/api/stores/popular",
                                "/api/products",
                                "/api/products/{id}",
                                "/api/products/featured",
                                "/api/categories",
                                "/api/categories/{id}",
                                "/api/categories/system",
                                "/api/categories/store",
                                "/api/search",
                                "/api/vouchers/available",
                                "/api/reviews",
                                "/api/reviews/product/{foodId}",
                                "/api/reviews/{id}/reply",
                                "/api/firebase/reset",
                                "/api/firebase/data"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
