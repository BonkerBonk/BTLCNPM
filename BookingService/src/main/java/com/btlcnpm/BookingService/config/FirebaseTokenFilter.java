package com.btlcnpm.BookingService.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;

@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseTokenFilter.class);

    @Autowired
    private FirebaseAuth firebaseAuth;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        logger.info("Authorization Header: " + header);

        // 1. Kiểm tra xem header có 'Authorization' và 'Bearer ' không
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Nếu không có, cho qua
            return;
        }

        String token = header.substring(7); // Cắt chuỗi "Bearer " để lấy token

        try {
            // 2. Xác thực token với Firebase
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            String uid = decodedToken.getUid();

            // 3. Nếu token hợp lệ, tạo 1 đối tượng Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(uid, null, null); // (principal, credentials, authorities)

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 4. Lưu đối tượng này vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (FirebaseAuthException e) {
            // Nếu token không hợp lệ
            logger.error("Firebase token verification failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response); // Chuyển request cho Filter tiếp theo
    }
}