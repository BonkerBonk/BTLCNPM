package com.btlcnpm.androidapp.ui.theme

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

// Bảng màu tối (Dark Theme) - Sử dụng màu mặc định Material 3
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
    // Bạn có thể tùy chỉnh các màu khác ở đây
    // background = Color(0xFF1C1B1F),
    // surface = Color(0xFF1C1B1F),
    // onPrimary = Color.Black,
    // onSecondary = Color.Black,
    // onTertiary = Color.Black,
    // onBackground = Color(0xFFFFFBFE),
    // onSurface = Color(0xFFFFFBFE),
)

// Bảng màu sáng (Light Theme) - Sử dụng màu mặc định Material 3
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
    // Bạn có thể tùy chỉnh các màu khác ở đây
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// Hàm Composable chính để áp dụng theme
@Composable
fun BTLCNPMTheme( // Đặt tên là BTLCNPMTheme như bạn đã import
    darkTheme: Boolean = isSystemInDarkTheme(), // Tự động phát hiện theme tối của hệ thống
    // Dynamic color chỉ có trên Android 12+
    dynamicColor: Boolean = true,
    // Tham số content là một @Composable lambda, chứa giao diện chính của bạn
    content: @Composable () -> Unit
) {
    // Xác định bảng màu sẽ sử dụng
    val colorScheme = when {
        // Nếu bật dynamic color và chạy trên Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // Lấy màu từ hình nền của người dùng
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Nếu dùng theme tối
        darkTheme -> DarkColorScheme
        // Nếu dùng theme sáng
        else -> LightColorScheme
    }

    // Lấy View hiện tại để tùy chỉnh thanh trạng thái (status bar)
    val view = LocalView.current
    if (!view.isInEditMode) { // Chỉ chạy khi ứng dụng đang chạy thực tế (không phải preview)
        // SideEffect dùng để thực hiện các hành động không liên quan trực tiếp đến Compose UI
        SideEffect {
            val window = (view.context as Activity).window
            // Đặt màu thanh trạng thái
            window.statusBarColor = colorScheme.primary.toArgb()
            // Đặt màu icon trên thanh trạng thái (sáng/tối)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Áp dụng MaterialTheme với bảng màu, kiểu chữ và nội dung đã chọn
    MaterialTheme(
        colorScheme = colorScheme, // Áp dụng bảng màu
        typography = Typography,   // Áp dụng kiểu chữ (Typography phải được định nghĩa trong Type.kt)
        content = content          // Hiển thị nội dung (AppNavigation) bên trong theme này
    )
}