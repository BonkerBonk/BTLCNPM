package com.btlcnpm.androidapp.ui.screens

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
        if (bookingState is BookingUiState.Success) {
            val bookingId = (bookingState as BookingUiState.Success).paymentResponse.bookingId
            // Thanh toán OK -> Chuyển màn hình
            navController.navigate(Screen.BookingSuccess.createRoute(bookingId)) {
                // Xóa tất cả màn hình từ MovieList đến đây
                popUpTo(Screen.MovieList.route) { inclusive = false }
            }
            bookingViewModel.resetBookingState() // Reset lại state
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

                Button(
                    onClick = {
                        bookingViewModel.startBookingFlow(showtimeId, quantity)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = bookingState !is BookingUiState.Loading
                ) {
                    Text("Xác nhận Thanh toán")
                }
            }

            // Lớp phủ Loading
            if (bookingState is BookingUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}