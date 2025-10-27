package com.btlcnpm.androidapp // Thay package của bạn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent // Hàm để hiển thị Compose UI
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.btlcnpm.androidapp.navigation.AppNavigation // Import NavHost của bạn
import com.btlcnpm.androidapp.ui.theme.BTLCNPMTheme // Import Theme (tên có thể khác)

class MainActivity : ComponentActivity() { // Kế thừa từ ComponentActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent là điểm bắt đầu để hiển thị giao diện Compose
        setContent {
            // Bọc toàn bộ ứng dụng trong Theme bạn đã định nghĩa (trong ui.theme)
            // Theme này sẽ cung cấp màu sắc, font chữ,... mặc định
            BTLCNPMTheme { // Đảm bảo tên Theme này đúng
                // Surface là một container cơ bản với màu nền từ Theme
                Surface(
                    modifier = Modifier.fillMaxSize(), // Chiếm toàn bộ màn hình
                    color = MaterialTheme.colorScheme.background // Lấy màu nền từ Theme
                ) {
                    // Gọi AppNavigation để hiển thị NavHost và màn hình đầu tiên (HomeScreen)
                    AppNavigation()
                }
            }
        }
    }
}