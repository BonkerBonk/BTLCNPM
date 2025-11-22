// com/btlcnpm/androidapp/ui/screens/VnpayPaymentScreen.kt
package com.btlcnpm.androidapp.ui.screens

import android.util.Log
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.btlcnpm.androidapp.navigation.Screen
import java.net.URL
import java.net.URLDecoder

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
    var isPaymentCompleted by remember { mutableStateOf(false) }
    var paymentStatus by remember { mutableStateOf("") } // "success", "failure", "pending"

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
            paymentStatus = "success"
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hiển thị thanh trạng thái
            when {
                paymentStatus == "success" -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Thanh toán thành công! Đang chuyển hướng...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                bookingState is BookingUiState.Error -> {
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
                }
                bookingState is BookingUiState.Loading -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            }

            // --- HIỂN THỊ WEBVIEW (nếu thanh toán chưa hoàn tất) ---
            if (paymentStatus != "success") {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                // Cho phép mixed content để load được resource từ HTTP trên HTTPS
                                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                    if (url == null) return false

                                    Log.d("VnpayWebView", "URL Loading: $url")

                                    // Kiểm tra nếu là callback URL từ VNPay
                                    if (isVnpayCallbackUrl(url)) {
                                        Log.d("VnpayWebView", "Detected VNPay callback URL")
                                        val responseCode = extractResponseCode(url)

                                        // 00 = Thành công, các mã khác là thất bại
                                        if (responseCode == "00") {
                                            Log.d("VnpayWebView", "Payment successful! Response code: $responseCode")
                                            paymentStatus = "success"
                                            // Polling sẽ tự động chạy để kiểm tra vé
                                        } else {
                                            Log.d("VnpayWebView", "Payment failed with code: $responseCode")
                                            paymentStatus = "failure"
                                        }
                                        return true // Ngăn WebView load URL này
                                    }

                                    // Cho phép các URL khác load bình thường (bao gồm OTP screen)
                                    return false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    Log.d("VnpayWebView", "Page finished loading: $url")
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: android.webkit.WebResourceRequest?,
                                    error: android.webkit.WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    Log.e("VnpayWebView", "Error loading: ${error?.description}")
                                }
                            }

                            // Load URL thanh toán
                            loadUrl(payUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Kiểm tra xem URL có phải là callback URL từ VNPay không.
 * VNPay sẽ redirect đến Return URL (mà bạn đã cấu hình) với các tham số phản hồi
 */
private fun isVnpayCallbackUrl(url: String): Boolean {
    // Kiểm tra các mẫu URL callback từ VNPay
    return url.contains("response_code") ||
            url.contains("vnp_ResponseCode") ||
            url.contains("vnp_TxnRef") ||
            url.contains("return-url") ||
            url.contains("payment-return") ||
            url.contains("vnpay-callback")
}

/**
 * Trích xuất mã phản hồi từ URL callback
 * VNPay trả về vnp_ResponseCode=00 (thành công) hoặc mã khác (thất bại)
 */
private fun extractResponseCode(url: String): String {
    return try {
        val uri = URL(url)
        val query = uri.query ?: ""

        Log.d("VnpayPaymentScreen", "Full URL: $url")
        Log.d("VnpayPaymentScreen", "Query string: $query")

        val params = mutableMapOf<String, String>()
        query.split("&").forEach { param ->
            if (param.contains("=")) {
                val parts = param.split("=", limit = 2)
                val key = parts[0]
                val value = if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else ""
                params[key] = value
                Log.d("VnpayPaymentScreen", "Param: $key = $value")
            }
        }

        // VNPay sử dụng vnp_ResponseCode
        val responseCode = params["vnp_ResponseCode"] ?: params["response_code"] ?: ""
        Log.d("VnpayPaymentScreen", "Response code extracted: $responseCode")
        responseCode
    } catch (e: Exception) {
        Log.e("VnpayPaymentScreen", "Error parsing response code: ${e.message}")
        ""
    }
}