package com.btlcnpm.androidapp.ui.screens
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.btlcnpm.androidapp.data.model.CreateReviewRequest
import com.btlcnpm.androidapp.data.model.Movie
import com.btlcnpm.androidapp.data.model.Review
import com.btlcnpm.androidapp.data.model.UserProfile

// Composable chính
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: String,
    movieViewModel: MovieViewModel = viewModel(factory = MovieViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory), // THÊM AuthViewModel
    onNavigateBack: () -> Unit,
    onNavigateToBooking: (String) -> Unit // THÊM Callback cho nút Đặt Vé
) {
    val movieDetailState by movieViewModel.movieDetailUiState.collectAsState()
    val reviewListState by movieViewModel.reviewListUiState.collectAsState() // THÊM State cho Review
    val profileState by authViewModel.profileUiState.collectAsState() // THÊM State cho Profile (lấy thông tin user)

    // Gọi API fetch chi tiết phim (ViewModel đã được sửa để tự động gọi fetch reviews)
    LaunchedEffect(key1 = movieId) {
        movieViewModel.fetchMovieById(movieId)
    }

    // Xóa state khi màn hình bị hủy (thoát ra)
    DisposableEffect(key1 = Unit) {
        onDispose {
            movieViewModel.clearMovieDetail() // ViewModel đã được sửa để clear cả review state
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (movieDetailState as? MovieDetailUiState.Success)?.movie?.title ?: "Chi Tiết Phim",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val state = movieDetailState) {
                is MovieDetailUiState.Initial, is MovieDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MovieDetailUiState.Error -> {
                    Text(
                        text = "Lỗi: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MovieDetailUiState.Success -> {
                    // Lấy thông tin profile
                    val profile = (profileState as? ProfileUiState.Success)?.profile

                    // Hiển thị nội dung chi tiết phim
                    MovieDetailContent(
                        movie = state.movie,
                        reviewState = reviewListState, // Truyền state review xuống
                        profile = profile, // Truyền profile xuống
                        onNavigateToBooking = {
                            // Gọi callback khi nhấn nút
                            onNavigateToBooking(state.movie.movieId!!)
                        },
                        onCreateReview = { rating, comment ->
                            // Xử lý khi người dùng gửi review
                            if (profile != null && profile.userId != null) {
                                val request = CreateReviewRequest(
                                    movieId = state.movie.movieId!!,
                                    userId = profile.userId,
                                    rating = rating,
                                    comment = comment.trim(),
                                    userFullName = profile.fullName ?: "Người dùng"
                                )
                                movieViewModel.createReview(request)
                            }
                        }
                    )
                }
            }
        }
    }
}

// Hàm helper để đổi Timestamp sang String (dd/MM/yyyy)
@RequiresApi(Build.VERSION_CODES.O)
private fun formatTimestamp(timestamp: com.btlcnpm.androidapp.data.model.FirestoreTimestamp?): String {
    if (timestamp?.seconds == null) return "N/A"
    try {
        val instant = Instant.ofEpochSecond(timestamp.seconds)
        val formatter = DateTimeFormatter
            .ofPattern("dd/MM/yyyy") // Định dạng ngày/tháng/năm
            .withZone(ZoneId.systemDefault()) // Dùng múi giờ của thiết bị
        return formatter.format(instant)
    } catch (e: Exception) {
        return "Invalid Date"
    }
}
// Composable chứa nội dung chi tiết phim
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MovieDetailContent(
    movie: Movie,
    reviewState: ReviewListUiState,
    profile: UserProfile?,
    onNavigateToBooking: () -> Unit,
    onCreateReview: (rating: Int, comment: String) -> Unit
) {
    // Dùng LazyColumn để nội dung có thể cuộn
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Không có padding ngang, để ảnh backdrop chiếm toàn bộ chiều rộng
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Ảnh Backdrop
        item {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.backdropUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Backdrop phim",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f), // Tỷ lệ 16:9
                contentScale = ContentScale.Crop
            )
        }

        // 2. Thông tin chính (Poster, Tên, Đạo diễn, Thể loại)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Poster
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.posterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = movie.title,
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(2f / 3f),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(16.dp))

                // Cột thông tin
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movie.title ?: "Không có tiêu đề",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    MovieInfoText(label = "Đạo diễn", value = movie.director)
                    Spacer(Modifier.height(4.dp))
                    MovieInfoText(label = "Thời lượng", value = movie.durationMinutes?.toString()?.plus(" phút"))
                    Spacer(Modifier.height(4.dp))
                    MovieInfoText(label = "Thể loại", value = movie.genre?.joinToString(", "))
                }
            }
        }

        // 3. Nút Đặt vé
        item {
            Button(
                onClick = onNavigateToBooking, // --- CẬP NHẬT ONCLICK ---
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp)
            ) {
                Text("Đặt Vé", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 4. Mô tả phim
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Nội dung phim",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = movie.description ?: "Không có mô tả.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp // Tăng khoảng cách dòng cho dễ đọc
                )
            }
        }

        // 5. Diễn viên
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Diễn viên",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = movie.cast?.joinToString(", ") ?: "Không có thông tin.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // --- 6. KHU VỰC ĐÁNH GIÁ MỚI ---
        item {
            ReviewsSection(
                reviewState = reviewState,
                profile = profile,
                onCreateReview = onCreateReview
            )
        }
    }
}

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

// --- CÁC COMPOSABLE MỚI CHO REVIEW ---

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewsSection(
    reviewState: ReviewListUiState,
    profile: UserProfile?,
    onCreateReview: (rating: Int, comment: String) -> Unit
) {
    var userRating by remember { mutableStateOf(0) }
    var userComment by remember { mutableStateOf(TextFieldValue("")) } // <<< SỬA 1: Dùng TextFieldValue

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "Đánh giá của khán giả",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        // --- Khu vực viết đánh giá ---
        if (profile != null) {
            Text("Viết đánh giá của bạn", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            // Chọn sao
            RatingBar(
                rating = userRating,
                onRatingChange = { userRating = it }
            )
            Spacer(Modifier.height(12.dp))
            // Ô bình luận
            OutlinedTextField(
                value = userComment,
                onValueChange = { userComment = it }, // Tự động dùng onValueChange(TextFieldValue)
                label = { Text("Bình luận của bạn...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    // <<< SỬA 2: Lấy text từ .text
                    if (userRating > 0 && userComment.text.isNotBlank()) {
                        onCreateReview(userRating, userComment.text) // <<< SỬA 3: Truyền .text
                        // Reset form
                        userRating = 0
                        userComment = TextFieldValue("") // <<< SỬA 4: Reset về TextFieldValue
                    }
                },
                // <<< SỬA 5: Kiểm tra .text
                enabled = userRating > 0 && userComment.text.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Gửi")
            }
        } else {
            Text("Bạn cần đăng nhập để viết đánh giá.")
        }

        Spacer(Modifier.height(24.dp))

        // --- Hiển thị danh sách đánh giá ---
        when (reviewState) {
            is ReviewListUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is ReviewListUiState.Error -> {
                Text("Lỗi tải đánh giá: ${reviewState.message}", color = MaterialTheme.colorScheme.error)
            }
            is ReviewListUiState.Success -> {
                if (reviewState.reviews.isEmpty()) {
                    Text("Chưa có đánh giá nào cho phim này.")
                } else {
                    reviewState.reviews.forEach { review ->
                        ReviewItem(review)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
            ReviewListUiState.Initial -> {} // Không làm gì
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewItem(review: Review) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = review.userFullName ?: "Người dùng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            // Hiển thị sao
            Row(verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { index ->
                    Icon(
                        imageVector = if (index <= (review.rating ?: 0.0)) Icons.Filled.Star else Icons.Outlined.Star, // <<< SỬA 2: Đổi .Filled thành .Outlined
                        contentDescription = "Rating Star",
                        tint = if (index <= (review.rating ?: 0.0)) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${review.rating ?: 0}/5",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = review.comment ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ngày: ${formatTimestamp(review.createdAt)}", // Chỉ lấy 10 ký tự đầu (YYYY-MM-DD)
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starCount: Int = 5,
    starColor: Color = Color(0xFFFFC107)
) {
    Row(modifier = modifier) {
        (1..starCount).forEach { index ->
            IconButton(onClick = { onRatingChange(index) }) {
                Icon(
                    imageVector = if (index <= rating) Icons.Filled.Star else Icons.Filled.Star,
                    contentDescription = "Star $index",
                    tint = if (index <= rating) starColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

