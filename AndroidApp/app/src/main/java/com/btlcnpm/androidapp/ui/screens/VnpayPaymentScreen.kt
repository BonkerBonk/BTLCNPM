// com/btlcnpm/androidapp/ui/screens/VnpayPaymentScreen.kt
package com.btlcnpm.androidapp.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView // <-- QUAN TRỌNG
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.btlcnpm.androidapp.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VnpayPaymentScreen(
    payUrl: String,
    bookingId: String,
    navController: NavController,
    bookingViewModel: BookingViewModel
) {
    val bookingState by bookingViewModel.bookingUiState.collectAsState()
    var isPolling by remember { mutableStateOf(false) }

    // Bắt đầu Polling (kiểm tra vé) khi màn hình được hiển thị
    LaunchedEffect(key1 = bookingId) {
        if (!isPolling) {
            isPolling = true
            bookingViewModel.startPollingBookingStatus(bookingId)
        }
    }

    // Lắng nghe kết quả Polling
    LaunchedEffect(key1 = bookingState) {
        if (bookingState is BookingUiState.MockSuccess) {
            // Polling thành công (vé đã được tạo)
            navController.navigate(Screen.BookingSuccess.createRoute(bookingId)) {
                // Xóa tất cả các màn hình từ MovieList (Home) đến đây
                popUpTo(Screen.MovieList.route) { inclusive = false }
            }
            bookingViewModel.resetBookingState()
        }
    }

    // Hủy Polling khi thoát màn hình
    DisposableEffect(key1 = Unit) {
        onDispose {
            // Reset state nếu thanh toán chưa thành công
            if (bookingState !is BookingUiState.MockSuccess) {
                bookingViewModel.resetBookingState()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán VNPay") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hiển thị thanh trạng thái Polling
            if (bookingState is BookingUiState.Error) {
                // Lỗi (ví dụ: hết giờ)
                Text(
                    text = (bookingState as BookingUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = {
                    bookingViewModel.resetBookingState()
                    navController.popBackStack()
                }) {
                    Text("Thử lại")
                }
            } else if (bookingState is BookingUiState.Loading) {
                // Đang polling...
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Đang chờ xác nhận thanh toán...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- HIỂN THỊ WEBVIEW ---
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient() // Client mặc định
                        loadUrl(payUrl) // Tải URL thanh toán
                    }
                },
                modifier = Modifier.fillMaxSize() // WebView chiếm phần còn lại
            )
        }
    }
}