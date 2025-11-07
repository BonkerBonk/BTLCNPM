package com.btlcnpm.androidapp.ui.theme // Đảm bảo đúng package

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Bảng màu cho Chế độ Tối (Dark Mode)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,         // Màu chính
    secondary = PurpleGrey80,   // Màu phụ
    tertiary = Pink80,          // Màu nhấn (Tertiary)
    background = DarkGray,      // Màu nền
    surface = DarkGray,         // Màu bề mặt (Card, Dialog,...)
    onPrimary = Purple40,       // Màu chữ/icon trên nền Primary
    onSecondary = PurpleGrey40, // Màu chữ/icon trên nền Secondary
    onTertiary = Pink40,        // Màu chữ/icon trên nền Tertiary
    onBackground = White,       // Màu chữ/icon trên nền Background
    onSurface = White,          // Màu chữ/icon trên nền Surface
    error = RedError            // Màu báo lỗi
    // Bạn có thể tùy chỉnh các màu khác như surfaceVariant, outline,...
)

// Bảng màu cho Chế độ Sáng (Light Mode)
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = White,         // Nền trắng
    surface = LightGray,        // Bề mặt màu xám nhạt
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Black,       // Chữ đen
    onSurface = Black,          // Chữ đen
    error = RedError

    /* Các màu mặc định khác sẽ được ghi đè nếu bạn định nghĩa ở đây */
)

// Composable function chính để áp dụng Theme
@Composable
fun AndroidAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Tự động phát hiện chế độ tối của hệ thống
    // Dynamic color chỉ khả dụng trên Android 12+
    dynamicColor: Boolean = true, // Cho phép sử dụng màu động từ hình nền (nếu hỗ trợ)
    content: @Composable () -> Unit // Nội dung của ứng dụng sẽ nằm trong này
) {
    // Chọn bảng màu phù hợp (sáng/tối, động/tĩnh)
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Lấy View hiện tại để kiểm soát thanh trạng thái (status bar)
    val view = LocalView.current
    if (!view.isInEditMode) { // Chỉ chạy khi ứng dụng thực sự chạy (không phải preview)
        // SideEffect dùng để thực hiện các thay đổi không thuộc Compose UI (như status bar)
        SideEffect {
            val window = (view.context as Activity).window
            // Đặt màu thanh trạng thái
            window.statusBarColor = colorScheme.primary.toArgb()
            // Đặt màu icon trên thanh trạng thái (sáng/tối)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Áp dụng MaterialTheme với bảng màu, kiểu chữ đã chọn
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Sử dụng Typography từ file Type.kt
        content = content        // Hiển thị nội dung ứng dụng bên trong Theme
    )
}