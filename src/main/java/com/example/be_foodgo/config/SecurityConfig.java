package com.example.be_foodgo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
<<<<<<< HEAD
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
=======
>>>>>>> 7c30aa5f062f8125b1f062c7287591fa09104286
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
<<<<<<< HEAD
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
=======
            .csrf(csrf -> csrf.disable()) // Tắt CSRF để dễ dàng test API POST/PUT/DELETE
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() // Tạm thời mở toàn bộ API để test
                .anyRequest().permitAll()
            );
        
>>>>>>> 7c30aa5f062f8125b1f062c7287591fa09104286
        return http.build();
    }
}
