package com.btlcnpm.BookingService.config;

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
    private FirebaseTokenFilter firebaseTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authz -> authz
                        // Bảo mật API tạo booking
                        .requestMatchers("/api/v1/booking/bookings").authenticated()
                        // Bảo mật API xem lịch sử
                        .requestMatchers("/api/v1/booking/my-history").authenticated()

                        // ===== CẬP NHẬT: Các API internal không cần bảo mật =====
                        // (được gọi từ các service khác, không từ client)
                        .requestMatchers("/api/v1/booking/internal/**").permitAll()

                        // Các API khác
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}