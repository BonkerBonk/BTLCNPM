package com.btlcnpm.androidapp.navigation // Thay package của bạn

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController // Cần import
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.btlcnpm.androidapp.ui.screens.auth.LoginScreen
import com.btlcnpm.androidapp.ui.screens.home.HomeScreen // Import màn hình Home

// Định nghĩa các route (chuỗi định danh) cho các màn hình
object Routes {
    const val HOME = "home_screen" // Đặt tên route rõ ràng
    // Thêm các route khác ở đây, ví dụ: const val DETAILS = "details_screen/{movieId}"
    const val LOGIN = "login_screen" // Đặt tên route rõ ràng

}

@Composable
fun AppNavigation() {
    // rememberNavController tạo và nhớ NavController qua các lần recompose
    val navController: NavHostController = rememberNavController()

    // NavHost là container chứa các màn hình (Composable destinations)
    NavHost(
        navController = navController, // Cung cấp NavController cho NavHost
        startDestination = Routes.HOME   // Màn hình sẽ hiển thị đầu tiên
    ) {

        // Định nghĩa màn hình Login
        composable(route = Routes.LOGIN) {
            // Gọi Composable LoginScreen
            LoginScreen(
                // Truyền vào lambda xử lý khi đăng nhập thành công
                onLoginSuccess = {
                    // Điều hướng đến màn hình Home
                    navController.navigate(Routes.HOME) {
                        // Xóa màn hình Login khỏi back stack để người dùng không quay lại được bằng nút Back
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
                // , onNavigateToRegister = { navController.navigate(Routes.REGISTER) } // Thêm sau
            )
        }
        // Khai báo một màn hình trong NavHost bằng hàm composable()
        composable(route = Routes.HOME) { // route là chuỗi định danh màn hình này
            // Gọi Composable HomeScreen khi route này được điều hướng đến
            HomeScreen(
                onMovieClick = { movieId ->
                    // Xử lý khi nhấn vào phim: điều hướng đến màn hình chi tiết
                    // navController.navigate("${Routes.DETAILS}/$movieId") // Ví dụ
                    println("Movie clicked: $movieId") // Tạm thời in ra Logcat
                }
            )
        }

        // Thêm các composable() khác cho các màn hình khác (Login, Details,...)
        /* Ví dụ:
        composable(route = Routes.DETAILS) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")
            // MovieDetailsScreen(movieId = movieId)
        }
        */
    }
}