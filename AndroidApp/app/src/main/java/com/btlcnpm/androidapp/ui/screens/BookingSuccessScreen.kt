package com.btlcnpm.androidapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.btlcnpm.androidapp.navigation.Screen
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BookingSuccessScreen(
    bookingId: String,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    LaunchedEffect(key1 = Unit) {
        authViewModel.fetchMyTickets()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF4CAF50), // Màu xanh lá
            modifier = Modifier.size(100.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Đặt vé thành công!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Mã đặt vé của bạn là:",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            bookingId,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Vui lòng kiểm tra vé của bạn trong mục 'Vé của tôi' ở trang cá nhân.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = {
                // Quay về trang chủ
                navController.navigate(Screen.MovieList.route) {
                    popUpTo(Screen.MovieList.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Về Trang Chủ")
        }
    }
}