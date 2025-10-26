package com.example.profile_service.controller; 

import com.example.profile_service.dto.UpdateProfileRequest; // MỚI
import com.example.profile_service.dto.UserProfileResponse;
import com.example.profile_service.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping; // MỚI
import org.springframework.web.bind.annotation.RequestBody; // MỚI
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    // --- HÀM LẤY HỒ SƠ (Giữ nguyên) ---
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uid = authentication.getPrincipal().toString();

            if (uid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Không tìm thấy thông tin xác thực."));
            }

            UserProfileResponse profile = profileService.getMyProfile(uid);
            return ResponseEntity.ok(profile);

        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // --- HÀM CẬP NHẬT HỒ SƠ (MỚI) ---
    @PutMapping("/me") // -> PUT /api/v1/profile/me
    public ResponseEntity<?> updateMyProfile(@RequestBody UpdateProfileRequest request) {
        
        try {
            // 1. Lấy UID (giống hệt như hàm GET)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uid = authentication.getPrincipal().toString();

            if (uid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Không tìm thấy thông tin xác thực."));
            }

            // 2. Gọi Service để CẬP NHẬT
            profileService.updateMyProfile(uid, request);

            // 3. (Theo hợp đồng API) Lấy lại hồ sơ vừa cập nhật và trả về [cite: 256]
            UserProfileResponse updatedProfile = profileService.getMyProfile(uid);
            
            return ResponseEntity.ok(updatedProfile); // Trả về 200 OK và hồ sơ mới

        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server khi cập nhật: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}