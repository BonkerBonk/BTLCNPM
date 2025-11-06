package com.btlcnpm.TicketService.config; // <<< SỬA LẠI TÊN PACKAGE

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private FirebaseTokenFilter firebaseTokenFilter; // (Tự động inject file bạn vừa tạo)

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
        // === SỬA ĐƯỜNG DẪN NÀY ===
        .requestMatchers("/api/v1/ticket/my-tickets").authenticated() // Chỉ bảo mật API này

            // Các API khác (như /internal/create) không cần bảo mật
            .anyRequest().permitAll()
        );

        return http.build();
    }
}