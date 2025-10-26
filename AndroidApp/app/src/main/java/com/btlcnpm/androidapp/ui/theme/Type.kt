package com.btlcnpm.androidapp.ui.theme // <- Đảm bảo package này đúng

import androidx.compose.material3.Typography // <<< Quan trọng: Import đúng Typography của Material 3
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Định nghĩa bộ kiểu chữ cho ứng dụng
// Bạn có thể tùy chỉnh font chữ, kích thước,... ở đây
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    /* Bạn có thể định nghĩa thêm các style khác như:
    headlineLarge, headlineMedium, titleMedium, bodyMedium, labelMedium, ...
    */
)