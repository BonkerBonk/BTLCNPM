package com.btlcnpm.androidapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.btlcnpm.androidapp.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VnpayPaymentScreen(
    payUrl: String,
    bookingId: String,
    navController: NavController,
    bookingViewModel: BookingViewModel = viewModel(factory = BookingViewModel.Factory)
) {
    val context = LocalContext.current
    val bookingState by bookingViewModel.bookingUiState.collectAsState()
    var isPolling by remember { mutableStateOf(false) }
    var isPaymentCompleted by remember { mutableStateOf(false) }

    // Mở Chrome Custom Tabs ngay khi màn hình được tạo
    LaunchedEffect(key1 = Unit) {
        try {
            Log.d("VnpayPaymentScreen", "Opening URL in Chrome Custom Tabs: $payUrl")

            val builder = CustomTabsIntent.Builder()
            builder.setShowTitle(true)
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(payUrl))

            Log.d("VnpayPaymentScreen", "Chrome Custom Tabs opened successfully")
        } catch (e: Exception) {
            Log.e("VnpayPaymentScreen", "Error opening Chrome Custom Tabs: ${e.message}")
        }
    }

    // Bắt đầu Polling khi màn hình được hiển thị
    LaunchedEffect(key1 = bookingId) {
        if (!isPolling) {
            isPolling = true
            bookingViewModel.startPollingBookingStatus(bookingId)
        }
    }

    // Lắng nghe kết quả Polling
    LaunchedEffect(key1 = bookingState) {
        if (bookingState is BookingUiState.MockSuccess && !isPaymentCompleted) {
            isPaymentCompleted = true
            // Sau 2 giây, chuyển sang màn Success
            kotlinx.coroutines.delay(2000)
            navController.navigate(Screen.BookingSuccess.createRoute(bookingId)) {
                popUpTo(Screen.MovieList.route) { inclusive = false }
            }
            bookingViewModel.resetBookingState()
        }
    }

    // Hủy Polling khi thoát màn hình
    DisposableEffect(key1 = Unit) {
        onDispose {
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
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon loading
            CircularProgressIndicator(modifier = Modifier.size(48.dp))

            Spacer(Modifier.height(24.dp))

            // Hiển thị trạng thái
            when {
                bookingState is BookingUiState.MockSuccess -> {
                    Text(
                        "✅ Thanh toán thành công!\nĐang chuyển hướng...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                bookingState is BookingUiState.Error -> {
                    Text(
                        text = (bookingState as BookingUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        bookingViewModel.resetBookingState()
                        navController.popBackStack()
                    }) {
                        Text("Thử lại")
                    }
                }
                bookingState is BookingUiState.Loading -> {
                    Text(
                        "Đang chờ xác nhận thanh toán...\n\nVui lòng hoàn tất thanh toán trong trình duyệt.\nSau khi thanh toán xong, vé sẽ tự động hiển thị.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Hướng dẫn
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "ℹ️ Hướng dẫn:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "1. Hoàn tất thanh toán trong trình duyệt\n" +
                                "2. Sau khi thanh toán thành công, quay lại app\n" +
                                "3. Vé của bạn sẽ tự động xuất hiện\n\n" +
                                "💡 Nếu không tự động chuyển, vui lòng bấm nút \"Quay lại\" và kiểm tra mục \"Vé của tôi\"",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Nút hủy
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hủy và Quay Lại")
            }
        }
    }
}