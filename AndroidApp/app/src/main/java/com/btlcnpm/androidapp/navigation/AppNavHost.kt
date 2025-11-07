package com.btlcnpm.androidapp.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log // <<< THÊM IMPORT NÀY
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.btlcnpm.androidapp.ui.screens.*
import androidx.lifecycle.viewmodel.compose.viewModel
import java.net.URLDecoder


// Composable chính quản lý việc điều hướng giữa các màn hình
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ComposableDestinationInComposeScope")
@Composable
fun AppNavHost(
    // NavController để điều khiển việc chuyển màn hình
    navController: NavHostController = rememberNavController(),
    // ViewModel chung cho Auth và Profile
    authViewModel: AuthViewModel,
    movieViewModel: MovieViewModel,
    theaterViewModel: TheaterViewModel
) {

    // === KHỞI TẠO BOOKING VIEWMODEL ===
    val bookingViewModel: BookingViewModel = viewModel(factory = BookingViewModel.Factory)

    // NavHost định nghĩa đồ thị điều hướng
    NavHost(
        navController = navController,
        // Màn hình bắt đầu khi mở ứng dụng
        startDestination = Screen.Login.route
    ) {
        // Định nghĩa Màn hình Đăng nhập
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccessNavigation = {
                    // Khi đăng nhập thành công, chuyển đến MovieList
                    navController.navigate(Screen.MovieList.route) {
                        // Xóa màn hình Login khỏi back stack (không quay lại được)
                        popUpTo(Screen.Login.route) { inclusive = true }
                        // Đảm bảo không tạo lại MovieList nếu nó đã có trên stack
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    // 1. Reset trạng thái lỗi (nếu có lỗi đăng nhập cũ)
                    authViewModel.resetAuthErrorState()
                    // 2. Điều hướng
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    // 1. Reset trạng thái lỗi
                    authViewModel.resetAuthErrorState()
                    // 2. Điều hướng
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccessNavigation = {
                    // 1. Thực hiện lệnh điều hướng quay về Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }

                    // 2. BƯỚC QUAN TRỌNG: Reset trạng thái của ViewModel
                    authViewModel.resetUiState()

                    // Optional: Hiển thị thông báo nhanh (Snackbar)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack() // Quay lại màn hình trước (Login)
                }
            )
        }

        // Định nghĩa Màn hình Danh sách Phim
        composable(Screen.MovieList.route) {
            MovieListScreen(
                movieViewModel = movieViewModel,
                onMovieClick = { movieId ->
                    navController.navigate(Screen.MovieDetail.createRoute(movieId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // Định nghĩa Màn hình Profile
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onLogoutNavigation = {
                    // Khi đăng xuất, quay lại màn hình Login
                    navController.navigate(Screen.Login.route) {
                        // Xóa tất cả các màn hình phía trên Login khỏi back stack
                        popUpTo(Screen.MovieList.route) { inclusive = true } // Hoặc popUpTo(0) để xóa hết
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.MovieDetail.route,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")
            if (movieId != null) {
                MovieDetailScreen(
                    movieId = movieId,
                    movieViewModel = movieViewModel,
                    authViewModel = authViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToBooking = { mid -> // <<< SỬA CHỖ NÀY
                        // Giờ chúng ta điều hướng thật sự
                        navController.navigate(Screen.SelectTheater.createRoute(mid))
                    }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(
            route = Screen.SelectTheater.route,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")

            SelectTheaterScreen(
                movieId = movieId,
                theaterViewModel = theaterViewModel,
                onNavigateBack = { navController.popBackStack() },
                onTheaterSelected = { theaterId, mid ->
                    // SỬA LẠI TODO: Điều hướng đến SelectShowtime
                    if (mid != null) {
                        navController.navigate(
                            Screen.SelectShowtime.createRoute(mid, theaterId)
                        )
                    } else {
                        // Xử lý lỗi nếu movieId bị null
                        Log.e("AppNavHost", "MovieId is null, cannot navigate to showtimes")
                    }
                }
            )
        }

        composable(
            route = Screen.SelectShowtime.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType },
                navArgument("theaterId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")
            val theaterId = backStackEntry.arguments?.getString("theaterId")
            if (movieId != null && theaterId != null) {
                SelectShowtimeScreen(
                    movieId = movieId,
                    theaterId = theaterId,
                    navController = navController,
                    bookingViewModel = bookingViewModel // Truyền VM vào
                )
            }
        }

        composable(
            route = Screen.BookingQuantity.route,
            arguments = listOf(
                navArgument("showtimeId") { type = NavType.StringType },
                navArgument("ticketPrice") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            BookingQuantityScreen(
                showtimeId = backStackEntry.arguments?.getString("showtimeId") ?: "",
                ticketPrice = backStackEntry.arguments?.getFloat("ticketPrice") ?: 0f,
                navController = navController,
                bookingViewModel = bookingViewModel // Truyền VM vào
            )
        }

        composable(
            route = Screen.BookingSuccess.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: "N/A"
            BookingSuccessScreen(
                bookingId = bookingId,
                navController = navController
            )
        }
        // === THÊM COMPOSABLE MỚI CHO VNPAY ===
        composable(
            route = Screen.VnpayPayment.route,
            arguments = listOf(
                navArgument("payUrl") { type = NavType.StringType },
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("payUrl") ?: ""
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""

            val payUrl = try {
                URLDecoder.decode(encodedUrl, "UTF-8")
            } catch (e: Exception) { "" }

            if (payUrl.isNotEmpty() && bookingId.isNotEmpty()) {
                VnpayPaymentScreen(
                    payUrl = payUrl,
                    bookingId = bookingId,
                    navController = navController,
                    bookingViewModel = bookingViewModel
                )
            } else {
                navController.popBackStack() // Quay lại nếu URL lỗi
            }
        }
    }
}

