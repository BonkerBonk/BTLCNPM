package com.example.auth_service.controller;

import com.example.auth_service.dto.ForgotPasswordRequest; // MỚI
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.LoginResponse;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.dto.RegisterResponse;
import com.example.auth_service.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // --- API REGISTER (Giữ nguyên) ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
    {
        // DÁN CODE NÀY VÀO:
        try {
            RegisterResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) { 
            if (e.getMessage() != null && e.getMessage().equals("Email đã tồn tại.")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", e.getMessage()));
            }
            // Trả về lỗi server nếu là 1 lỗi RuntimeException khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi runtime: " + e.getMessage()));

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Lỗi Firebase: " + e.getMessage()));
            
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server: " + e.getMessage()));
        }
        // KẾT THÚC CODE DÁN

    } // Đây là dấu } của hàm registerUser (dòng 33)
    }

    // --- API LOGIN (Giữ nguyên) ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
    {
        // DÁN CODE NÀY VÀO:
        try {
            LoginResponse response = authService.login(request);
            
            // Hợp đồng API: Trả về 200 OK
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Bắt lỗi "Email hoặc mật khẩu không chính xác."
            if (e.getMessage() != null && e.getMessage().equals("Email hoặc mật khẩu không chính xác.")) {
                // Hợp đồng API: Trả về 401 Unauthorized
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED) 
                        .body(Map.of("message", e.getMessage()));
            }
            // Các lỗi runtime khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi runtime: " + e.getMessage()));
        }
        // KẾT THÚC CODE DÁN

    } // Đây là dấu } của hàm loginUser (dòng 40)
    }

    // --- API FORGOT PASSWORD (MỚI) ---
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String message = authService.forgotPassword(request.getEmail());
            
            // Hợp đồng API: Luôn trả về 200 OK (kể cả khi email không tồn tại)
            return ResponseEntity.ok(Map.of("message", message));

        } catch (RuntimeException e) {
            // Bắt các lỗi runtime khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}