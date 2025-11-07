package com.btlcnpm.androidapp.ui.theme // Đảm bảo đúng package

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Định nghĩa các kiểu chữ cho ứng dụng
// Bạn có thể tùy chỉnh font family, size, weight theo thiết kế
val Typography = Typography(
    // Kiểu chữ cho nội dung thông thường
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // Font mặc định của hệ thống
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, // Kích thước chữ
        lineHeight = 24.sp, // Chiều cao dòng
        letterSpacing = 0.5.sp // Khoảng cách giữa các chữ
    ),
    // Kiểu chữ cho tiêu đề lớn (ví dụ: tên màn hình)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // In đậm
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Kiểu chữ cho tiêu đề trung bình
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // Hơi đậm
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Kiểu chữ cho nhãn nhỏ (ví dụ: label của TextField)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // Bạn có thể định nghĩa thêm các kiểu khác như bodyMedium, bodySmall,
    // headlineLarge, headlineMedium, headlineSmall,... nếu cần.
    // Ví dụ:
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
)