package com.btlcnpm.androidapp.ui.screens

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.btlcnpm.androidapp.data.model.Ticket
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketQRScreen(
    ticket: Ticket,
    navController: NavController
) {
    // Tạo QR Code từ ticketId
    val qrBitmap = remember(ticket.ticketId) {
        generateQRCode(ticket.qrCodeData, 512)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mã QR Vé") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thông tin phim
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = ticket.movieTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${ticket.theaterName} - ${ticket.roomName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Suất: ${formatShowtime(ticket.showtimeStartTime)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // QR Code
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(300.dp)
                        )
                    } else {
                        CircularProgressIndicator()
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Mã QR (Dữ liệu):",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = ticket.qrCodeData,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
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
                        "💡 Hướng dẫn:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "• Xuất trình mã QR này tại quầy vé\n" +
                                "• Nhân viên sẽ quét để xác nhận vé\n" +
                                "• Mỗi mã QR chỉ được sử dụng 1 lần",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Trạng thái vé
            val statusColor = when (ticket.status) {
                "VALID" -> MaterialTheme.colorScheme.primary
                "USED" -> MaterialTheme.colorScheme.error
                "EXPIRED" -> MaterialTheme.colorScheme.outline
                else -> MaterialTheme.colorScheme.onSurface
            }

            val statusText = when (ticket.status) {
                "VALID" -> "✅ Vé hợp lệ"
                "USED" -> "❌ Đã sử dụng"
                "EXPIRED" -> "⏰ Đã hết hạn"
                else -> ticket.status
            }

            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Hàm tạo QR Code
private fun generateQRCode(data: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

// Hàm format thời gian
@RequiresApi(Build.VERSION_CODES.O)
private fun formatShowtime(timestamp: com.btlcnpm.androidapp.data.model.FirestoreTimestamp?): String {
    if (timestamp?.seconds == null) return "N/A"
    return try {
        val instant = Instant.ofEpochSecond(timestamp.seconds)
        val formatter = DateTimeFormatter
            .ofPattern("HH:mm - dd/MM/yyyy")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        "Invalid Date"
    }
}