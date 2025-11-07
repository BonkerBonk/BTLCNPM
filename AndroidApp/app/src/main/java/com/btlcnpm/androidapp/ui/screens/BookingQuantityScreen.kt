package com.btlcnpm.androidapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.btlcnpm.androidapp.navigation.Screen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingQuantityScreen(
    showtimeId: String,
    ticketPrice: Float,
    navController: NavController,
    bookingViewModel: BookingViewModel = viewModel(factory = BookingViewModel.Factory)
) {
    var quantity by remember { mutableStateOf(1) }
    val totalPrice = quantity * ticketPrice

    val bookingState by bookingViewModel.bookingUiState.collectAsState()

    // Lắng nghe trạng thái thanh toán
    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is BookingUiState.MockSuccess -> {
                // Thanh toán MOCK thành công -> Chuyển màn hình Success
                navController.navigate(Screen.BookingSuccess.createRoute(state.bookingId)) {
                    popUpTo(Screen.MovieList.route) { inclusive = false }
                }
                bookingViewModel.resetBookingState()
            }
            is BookingUiState.AwaitingMomoPayment -> {
                // ĐÃ CÓ QR MOMO -> Chuyển màn hình MoMo
                // (Giả sử bạn đã tạo màn hình này)
                // navController.navigate(Screen.MomoPayment.createRoute(state.qrCodeUrl, state.bookingId))

                // Tạm thời reset (vì chưa làm màn MoMo)
                bookingViewModel.resetBookingState()
                Log.d("Booking", "Nhận được QR MoMo (chưa có màn hình)")
            }
            // === LOGIC MỚI CHO VNPAY ===
            is BookingUiState.AwaitingVnpayPayment -> {
                // ĐÃ CÓ URL VNPAY -> Chuyển màn hình VnpayPayment
                navController.navigate(
                    Screen.VnpayPayment.createRoute(state.payUrl, state.bookingId)
                )
                // Không reset state vội
            }
            else -> {}
        }
    }

    // Định dạng tiền tệ
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn Số Lượng Vé") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Giá vé: ${currencyFormat.format(ticketPrice)}/vé",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Bộ chọn số lượng
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { if (quantity > 1) quantity-- },
                        enabled = quantity > 1 && bookingState !is BookingUiState.Loading
                    ) {
                        Text("-", fontSize = 24.sp)
                    }
                    Text(
                        "$quantity",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { quantity++ }, // (Nâng cao: check với `availableTickets`)
                        enabled = bookingState !is BookingUiState.Loading
                    ) {
                        Text("+", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    "Tổng tiền",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    currencyFormat.format(totalPrice),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.weight(1f)) // Đẩy nút xuống dưới

                // Hiển thị thông báo lỗi
                if (bookingState is BookingUiState.Error) {
                    Text(
                        (bookingState as BookingUiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // === THÊM NÚT VNPAY ===

                // Nút 1: Thanh toán MoMo
                Button(
                    onClick = {
                        bookingViewModel.startBookingFlow(showtimeId, quantity, "MOMO_QR")
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = bookingState !is BookingUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAE0069)) // Màu MoMo
                ) {
                    Text("Thanh toán bằng MoMo QR")
                }

                Spacer(Modifier.height(12.dp))

                // Nút 2: Thanh toán VNPay
                Button(
                    onClick = {
                        bookingViewModel.startBookingFlow(showtimeId, quantity, "VNPAY")
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = bookingState !is BookingUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005A9C)) // Màu xanh VNPay
                ) {
                    Text("Thanh toán bằng VNPay")
                }

                Spacer(Modifier.height(12.dp))

                // Nút 3: Thanh toán Mock (để test)
                OutlinedButton(
                    onClick = {
                        bookingViewModel.startBookingFlow(showtimeId, quantity, "MOCK_SUCCESS")
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = bookingState !is BookingUiState.Loading
                ) {
                    Text("Thanh toán Giả lập (Mock Success)")
                }
            }

            // Lớp phủ Loading
            if (bookingState is BookingUiState.Loading) {
                Column(
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Đang tạo đơn hàng...", textAlign = TextAlign.Center)
                }
            }
        }
    }
}