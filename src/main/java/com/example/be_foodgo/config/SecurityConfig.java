package com.example.be_foodgo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF để dễ dàng test API POST/PUT/DELETE
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() // Tạm thời mở toàn bộ API để test
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}
