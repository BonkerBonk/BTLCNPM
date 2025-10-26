package com.btlcnpm.androidapp.ui.screens.home // Thay package của bạn

import androidx.compose.foundation.layout.* // Column, Box, PaddingValues, Modifier
import androidx.compose.foundation.lazy.grid.GridCells // Để tạo lưới
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // Grid cuộn dọc
import androidx.compose.foundation.lazy.grid.items // Để duyệt item trong Grid
import androidx.compose.material3.* // Scaffold, TopAppBar, TabRow, Tab, CircularProgressIndicator, Text
import androidx.compose.runtime.* // Composable, remember, mutableStateOf, collectAsState, State
import androidx.compose.ui.Alignment // Căn chỉnh nội dung trong Box
import androidx.compose.ui.Modifier // Tùy chỉnh Composable
import androidx.compose.ui.unit.dp // Đơn vị dp
import androidx.lifecycle.viewmodel.compose.viewModel // Hàm để lấy ViewModel
import com.btlcnpm.androidapp.ui.common.MoviePosterItem // Import item phim đã tạo

// @OptIn để dùng các API Material 3 có thể thay đổi trong tương lai
@OptIn(ExperimentalMaterial3Api::class)
@Composable // Đánh dấu hàm vẽ giao diện
fun HomeScreen(
    // Callback này sẽ được gọi khi người dùng nhấn vào một MoviePosterItem
    // Nó nhận movieId và sẽ được dùng để điều hướng sang màn hình chi tiết
    onMovieClick: (movieId: String) -> Unit,
    // Lấy HomeViewModel bằng hàm viewModel()
    // Compose sẽ tự động tạo và quản lý vòng đời ViewModel này
    viewModel: HomeViewModel = viewModel()
) {
    // Sử dụng collectAsState để lắng nghe StateFlow từ ViewModel
    // Biến này sẽ tự động cập nhật và làm giao diện recompose khi StateFlow thay đổi
    val nowShowingMovies by viewModel.nowShowingMovies.collectAsState()
    val comingSoonMovies by viewModel.comingSoonMovies.collectAsState()
    val isLoadingNowShowing by viewModel.isLoadingNowShowing.collectAsState()
    val isLoadingComingSoon by viewModel.isLoadingComingSoon.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Sử dụng remember và mutableIntStateOf để lưu trạng thái của tab đang được chọn
    // remember giúp giữ giá trị này qua các lần recompose
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Đang chiếu", "Sắp chiếu") // Danh sách tiêu đề các tab

    // Scaffold cung cấp cấu trúc layout chuẩn của Material Design
    Scaffold(
        // TopAppBar là thanh công cụ ở trên cùng
        topBar = {
            TopAppBar(title = { Text("Phim") }) // Tiêu đề đơn giản
            // Bạn có thể thêm actions = { ... } để thêm icon tìm kiếm, thông báo
        }
    ) { innerPadding -> // innerPadding chứa giá trị padding cần áp dụng để nội dung không bị che bởi TopAppBar

        // Column xếp các thành phần giao diện theo chiều dọc
        Column(modifier = Modifier.padding(innerPadding)) { // Áp dụng padding từ Scaffold

            // TabRow hiển thị các tab
            TabRow(selectedTabIndex = selectedTabIndex) {
                // Duyệt qua danh sách tiêu đề tab
                tabs.forEachIndexed { index, title ->
                    // Tab là một thành phần con của TabRow
                    Tab(
                        selected = (selectedTabIndex == index), // Cho biết tab này có đang được chọn
                        onClick = { selectedTabIndex = index }, // Cập nhật state khi nhấn vào tab
                        text = { Text(title) } // Hiển thị tiêu đề
                    )
                }
            }

            // Xác định danh sách phim và trạng thái loading dựa vào tab được chọn
            val currentMovies = if (selectedTabIndex == 0) nowShowingMovies else comingSoonMovies
            val isLoading = if (selectedTabIndex == 0) isLoadingNowShowing else isLoadingComingSoon

            // Box dùng để hiển thị một trong ba trạng thái: Loading, Lỗi, hoặc Danh sách phim
            Box(
                modifier = Modifier.fillMaxSize(), // Chiếm toàn bộ không gian còn lại
                contentAlignment = Alignment.Center // Căn giữa nội dung (đặc biệt hữu ích cho Loading/Lỗi)
            ) {
                // Sử dụng when để kiểm tra trạng thái và hiển thị UI tương ứng
                when {
                    // Nếu đang tải -> Hiển thị vòng quay
                    isLoading -> {
                        CircularProgressIndicator() // Composable hiển thị vòng quay loading
                    }
                    // Nếu có lỗi -> Hiển thị thông báo lỗi
                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "Lỗi không xác định.", // Lấy thông báo lỗi, nếu null thì hiển thị mặc định
                            color = MaterialTheme.colorScheme.error, // Sử dụng màu báo lỗi từ theme
                            modifier = Modifier.padding(16.dp) // Thêm padding
                        )
                    }
                    // Nếu không tải, không lỗi, nhưng danh sách rỗng -> Báo không có phim
                    currentMovies.isEmpty() && !isLoading -> {
                        Text(text = "Hiện chưa có phim nào.", modifier = Modifier.padding(16.dp))
                    }
                    // Nếu không có vấn đề gì -> Hiển thị lưới phim
                    else -> {
                        // LazyVerticalGrid là Composable hiệu quả để hiển thị danh sách dạng lưới
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3), // Chia lưới thành 3 cột cố định
                            modifier = Modifier.fillMaxSize(), // Chiếm hết không gian Box
                            contentPadding = PaddingValues(4.dp) // Padding nhỏ quanh toàn bộ lưới
                        ) {
                            // items là hàm để cung cấp dữ liệu cho LazyVerticalGrid
                            items(
                                items = currentMovies, // Danh sách phim cần hiển thị
                                // key giúp Compose xác định item nào đã thay đổi, thêm, xóa
                                // để tối ưu việc vẽ lại, nên dùng ID duy nhất của dữ liệu
                                key = { movie -> movie.movieId }
                            ) { movieData -> // movieData là một MovieDTO trong danh sách
                                // Gọi MoviePosterItem để vẽ giao diện cho từng phim
                                MoviePosterItem(
                                    movie = movieData,
                                    onClick = onMovieClick // Truyền hàm onMovieClick xuống item
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}