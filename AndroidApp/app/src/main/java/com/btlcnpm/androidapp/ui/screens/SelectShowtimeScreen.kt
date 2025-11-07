package com.btlcnpm.androidapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.btlcnpm.androidapp.data.model.Showtime
import com.btlcnpm.androidapp.navigation.Screen
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectShowtimeScreen(
    movieId: String,
    theaterId: String,
    navController: NavController,
    bookingViewModel: BookingViewModel = viewModel(factory = BookingViewModel.Factory)
) {
    // Gọi load showtimes khi màn hình được tạo
    LaunchedEffect(key1 = movieId, key2 = theaterId) {
        bookingViewModel.loadShowtimes(movieId, theaterId)
    }

    val uiState by bookingViewModel.showtimeUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn Suất Chiếu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ShowtimeUiState.Loading, ShowtimeUiState.Idle -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ShowtimeUiState.Error -> {
                    Text(
                        text = "Lỗi: ${state.message}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ShowtimeUiState.Success -> {
                    if (state.showtimes.isEmpty()) {
                        Text(
                            text = "Không có suất chiếu nào cho phim này tại rạp này.",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    } else {
                        // Hiển thị các suất chiếu
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 100.dp), // Tự động chia cột
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.showtimes) { showtime ->
                                ShowtimeChip(
                                    showtime = showtime,
                                    onClick = {
                                        // Chuyển sang màn hình chọn số lượng
                                        navController.navigate(
                                            Screen.BookingQuantity.createRoute(
                                                showtime.showtimeId,
                                                showtime.ticketPrice.toFloat()
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowtimeChip(showtime: Showtime, onClick: () -> Unit) {
    // Định dạng giờ:phút
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = try {
        // Chuyển String ISO 8601 sang đối tượng Instant
        val instant = Instant.parse(showtime.startTime)
        // Chuyển sang múi giờ địa phương (quan trọng)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        localDateTime.format(timeFormatter)
    } catch (e: Exception) {
        "Lỗi giờ"
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = startTime,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Còn ${showtime.availableTickets} vé",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp
            )
        }
    }
}