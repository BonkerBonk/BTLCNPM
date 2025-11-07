package com.btlcnpm.androidapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.btlcnpm.androidapp.data.model.Theater

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTheaterScreen(
    movieId: String?, // Nhận movieId để có thể truyền đi tiếp
    theaterViewModel: TheaterViewModel = viewModel(factory = TheaterViewModel.Factory),
    onNavigateBack: () -> Unit,
    onTheaterSelected: (theaterId: String, movieId: String?) -> Unit // Callback khi chọn rạp
) {
    val theaterState by theaterViewModel.theaterListUiState.collectAsState()
    val searchQuery by theaterViewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn Rạp Chiếu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Thanh tìm kiếm theo thành phố
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { theaterViewModel.onSearchQueryChanged(it) },
                label = { Text("Tìm theo thành phố (ví dụ: Hà Nội)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // Hiển thị danh sách rạp
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = theaterState) {
                    is TheaterListUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is TheaterListUiState.Error -> {
                        Text(
                            text = "Lỗi: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    }
                    is TheaterListUiState.Success -> {
                        if (state.theaters.isEmpty()) {
                            Text(
                                text = "Không tìm thấy rạp nào.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.theaters, key = { it.theaterId ?: it.hashCode() }) { theater ->
                                    TheaterItem(
                                        theater = theater,
                                        onClick = {
                                            theater.theaterId?.let {
                                                // Bắn sự kiện chọn rạp, gửi cả theaterId và movieId
                                                onTheaterSelected(it, movieId)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is TheaterListUiState.Idle -> {
                        // Trạng thái chờ
                    }
                }
            }
        }
    }
}

@Composable
fun TheaterItem(theater: Theater, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = theater.name ?: "Tên rạp không xác định",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Địa chỉ: ${theater.address ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Thành phố: ${theater.city ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}