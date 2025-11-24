package com.btlcnpm.androidapp.navigation

// Định nghĩa các route (đường dẫn) cho các màn hình trong ứng dụng
sealed class Screen(val route: String) {
    // Màn hình Đăng nhập
    object Login : Screen("login_screen")

    // Màn hình Thông tin cá nhân
    object Profile : Screen("profile_screen")

    // Màn hình Danh sách phim
    object MovieList : Screen("movie_list_screen")

    // Màn hình Chi tiết phim - Route này có tham số {movieId}
    object MovieDetail : Screen("movie_detail_screen/{movieId}") {
        // Hàm helper để tạo route hoàn chỉnh với movieId cụ thể
        // Ví dụ: Screen.MovieDetail.createRoute("m_001") -> "movie_detail_screen/m_001"
        fun createRoute(movieId: String) = "movie_detail_screen/$movieId"
    }
    object Register : Screen("register_screen") // Màn hình Đăng ký
    object ForgotPassword : Screen("forgot_password_screen") // Màn hình Quên mật khẩu

    // === BƯỚC 4: THÊM ROUTE MỚI CHO CHỌN RẠP ===
    object SelectTheater : Screen("select_theater_screen/{movieId}") {
        fun createRoute(movieId: String) = "select_theater_screen/$movieId"
    }
    // === KẾT THÚC BƯỚC 4 ===
    object SelectShowtime : Screen("select_showtime_screen/{movieId}/{theaterId}") {
        fun createRoute(movieId: String, theaterId: String) =
            "select_showtime_screen/$movieId/$theaterId"
    }

    object BookingQuantity : Screen("booking_quantity_screen/{showtimeId}/{ticketPrice}") {
        fun createRoute(showtimeId: String, ticketPrice: Float) =
            "booking_quantity_screen/$showtimeId/$ticketPrice"
    }

    object BookingSuccess : Screen("booking_success_screen/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_success_screen/$bookingId"
    }
    // Route cho VNPay
    object VnpayPayment : Screen("vnpay_payment_screen/{payUrl}/{bookingId}") {
        fun createRoute(payUrl: String, bookingId: String): String {
            val encodedUrl = java.net.URLEncoder.encode(payUrl, "UTF-8")
            return "vnpay_payment_screen/$encodedUrl/$bookingId"
        }
    }
    object TicketQR : Screen("ticket_qr_screen/{ticketId}") {
        fun createRoute(ticketId: String) = "ticket_qr_screen/$ticketId"
    }

}
    
