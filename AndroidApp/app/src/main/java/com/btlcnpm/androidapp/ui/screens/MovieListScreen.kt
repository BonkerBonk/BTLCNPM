package com.btlcnpm.androidapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons // <<< IMPORT NÀY
import androidx.compose.material.icons.filled.AccountCircle // <<< THÊM IMPORT ICON PROFILE
import androidx.compose.material.icons.filled.Search // <<< IMPORT NÀY
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.btlcnpm.androidapp.data.model.Movie
import com.btlcnpm.androidapp.data.model.MovieSearchDTO // <<< IMPORT NÀY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    movieViewModel: MovieViewModel = viewModel(factory = MovieViewModel.Factory),
    onMovieClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit // <<< THÊM THAM SỐ MỚI
) {
    // Lấy state của danh sách phim (khi không tìm kiếm)
    val movieListState by movieViewModel.movieListUiState.collectAsState()

    // === LẤY STATE CỦA TÌM KIẾM ===
    val searchQuery by movieViewModel.searchQuery.collectAsState()
    val movieSearchState by movieViewModel.movieSearchUiState.collectAsState()
    // === KẾT THÚC LẤY STATE ===

    // Chỉ gọi API fetchAllMovies nếu không có tìm kiếm (chạy 1 lần)
    LaunchedEffect(Unit) {
        if (searchQuery.isBlank()) {
            movieViewModel.fetchAllMovies()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh Sách Phim") },
                // === THÊM KHỐI actions CHO ICON PROFILE ===
                actions = {
                    IconButton(onClick = onNavigateToProfile) { // Gọi callback khi click
                        Icon(
                            imageVector = Icons.Filled.AccountCircle, // Sử dụng icon tài khoản
                            contentDescription = "Trang cá nhân"
                        )
                    }
                }
                // === KẾT THÚC KHỐI actions ===
            )
        }
    ) { paddingValues ->
        // Sử dụng Column để chứa SearchBar và LazyColumn
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // === 1. THANH TÌM KIẾM ===
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { movieViewModel.onSearchQueryChanged(it) },
                label = { Text("Tìm kiếm phim...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // === 2. HIỂN THỊ DANH SÁCH (TÌM KIẾM hoặc TẤT CẢ) ===

            // Kiểm tra xem có đang tìm kiếm không
            val showSearchResults = searchQuery.isNotBlank()

            if (showSearchResults) {
                // --- HIỂN THỊ KẾT QUẢ TÌM KIẾM ---
                when (val state = movieSearchState) {
                    is MovieSearchUiState.Idle -> {
                        // Trạng thái chờ (khi query < 3 chữ cái)
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Gõ ít nhất 3 chữ cái để tìm kiếm...")
                        }
                    }
                    is MovieSearchUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is MovieSearchUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Lỗi tìm kiếm: ${state.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is MovieSearchUiState.Success -> {
                        if (state.results.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Không tìm thấy kết quả nào.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.results, key = { it.movieId ?: it.hashCode() }) { movieDto ->
                                    // Tạo Composable mới cho kết quả tìm kiếm (đơn giản hơn)
                                    MovieSearchItem(movie = movieDto, onClick = {
                                        movieDto.movieId?.let { id -> onMovieClick(id) }
                                    })
                                }
                            }
                        }
                    }
                }
            } else {
                // --- HIỂN THỊ TẤT CẢ PHIM (như cũ) ---
                when (val state = movieListState) {
                    is MovieListUiState.Initial, is MovieListUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is MovieListUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Lỗi: ${state.message}", color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { movieViewModel.fetchAllMovies() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                    is MovieListUiState.Success -> {
                        if (state.movies.isEmpty()) {
                            Text("Không có phim nào.", modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.movies, key = { it.movieId ?: it.hashCode() }) { movie ->
                                    MovieListItem(movie = movie, onClick = {
                                        movie.movieId?.let { id -> onMovieClick(id) }
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Composable cho một item phim trong danh sách
@Composable
fun MovieListItem(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Sử dụng clickable từ androidx.compose.foundation để xử lý click
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Ảnh Poster (sử dụng Coil)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title ?: "Movie Poster",
                modifier = Modifier
                    .width(90.dp)
                    .aspectRatio(2f / 3f)
                    .heightIn(max = 135.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            // Thông tin phim
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title ?: "Không có tiêu đề",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))

                MovieInfoText(label = "Đạo diễn", value = movie.director)
                Spacer(Modifier.height(2.dp))
                MovieInfoText(label = "Thời lượng", value = movie.durationMinutes?.toString()?.plus(" phút"))
                Spacer(Modifier.height(2.dp))
                MovieInfoText(label = "Thể loại", value = movie.genre?.joinToString(", "))
            }
        }
    }
}

// Composable phụ trợ để hiển thị thông tin phim
@Composable
private fun MovieInfoText(label: String, value: String?) {
    Text(
        text = "$label: ${value ?: "N/A"}",
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun MovieSearchItem(movie: MovieSearchDTO, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically // Căn giữa
        ) {
            // Ảnh Poster
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title ?: "Movie Poster",
                modifier = Modifier
                    .width(60.dp) // Poster nhỏ hơn cho kết quả tìm kiếm
                    .aspectRatio(2f / 3f),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            // Chỉ hiển thị tên phim
            Text(
                text = movie.title ?: "Không có tiêu đề",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
