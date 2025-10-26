package com.example.profile_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Kích hoạt Spring Security
public class SecurityConfig {

    @Autowired
    private FirebaseTokenFilter firebaseTokenFilter; // Tiêm "người gác cổng"

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt tính năng CSRF (vì dùng token, không dùng session)
            .csrf(csrf -> csrf.disable())

            // 2. Không tạo hoặc dùng Session (STATELESS)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. Lắp "người gác cổng" (FirebaseTokenFilter) vào TRƯỚC bộ lọc mặc định
            .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class)

            // 4. Định nghĩa luật lệ
            .authorizeHttpRequests(authz -> authz
                // Tất cả các request bắt đầu bằng "/api/v1/profile/"
                .requestMatchers("/api/v1/profile/**").authenticated() 
                
                // Bất kỳ request nào khác (ví dụ: /) thì không cần
                .anyRequest().permitAll()
            );

        return http.build();
    }
}