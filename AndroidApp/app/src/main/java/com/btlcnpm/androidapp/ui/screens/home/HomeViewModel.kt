package com.btlcnpm.androidapp.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Coroutine scope gắn với ViewModel
import com.btlcnpm.androidapp.data.MovieDTO
import com.btlcnpm.androidapp.data.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow // StateFlow có thể thay đổi giá trị
import kotlinx.coroutines.flow.StateFlow         // StateFlow chỉ đọc (an toàn cho UI)
import kotlinx.coroutines.flow.asStateFlow       // Chuyển Mutable thành StateFlow chỉ đọc
import kotlinx.coroutines.launch                 // Hàm để chạy coroutine

// Enum để phân biệt loại phim, giúp code rõ ràng hơn
enum class MovieType {
    NOW_SHOWING, COMING_SOON
}

// Kế thừa từ ViewModel()
class HomeViewModel(
    // Tạm thời tạo Repository ở đây, cách tốt nhất là dùng DI (Hilt/Koin)
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    // _nowShowingMovies: StateFlow nội bộ, có thể thay đổi giá trị trong ViewModel
    private val _nowShowingMovies = MutableStateFlow<List<MovieDTO>>(emptyList())
    // nowShowingMovies: StateFlow công khai, UI sẽ lắng nghe cái này, không sửa được từ UI
    val nowShowingMovies: StateFlow<List<MovieDTO>> = _nowShowingMovies.asStateFlow()

    private val _comingSoonMovies = MutableStateFlow<List<MovieDTO>>(emptyList())
    val comingSoonMovies: StateFlow<List<MovieDTO>> = _comingSoonMovies.asStateFlow()

    // State để theo dõi trạng thái loading cho từng danh sách
    private val _isLoadingNowShowing = MutableStateFlow(false)
    val isLoadingNowShowing: StateFlow<Boolean> = _isLoadingNowShowing.asStateFlow()

    private val _isLoadingComingSoon = MutableStateFlow(false)
    val isLoadingComingSoon: StateFlow<Boolean> = _isLoadingComingSoon.asStateFlow()

    // State để lưu trữ thông báo lỗi (nếu có)
    private val _errorMessage = MutableStateFlow<String?>(null) // String? nghĩa là có thể null (không có lỗi)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Khối init được gọi ngay khi ViewModel được tạo
    init {
        // Tải dữ liệu ban đầu cho cả hai danh sách
        loadMovies(MovieType.NOW_SHOWING)
        loadMovies(MovieType.COMING_SOON)
    }

    // Hàm công khai để UI (hoặc init) gọi tải dữ liệu
    fun loadMovies(type: MovieType) {
        // viewModelScope.launch đảm bảo coroutine bị hủy khi ViewModel bị hủy
        viewModelScope.launch {
            // Xác định StateFlow nào cần được cập nhật
            val loadingState = if (type == MovieType.NOW_SHOWING) _isLoadingNowShowing else _isLoadingComingSoon
            val movieState = if (type == MovieType.NOW_SHOWING) _nowShowingMovies else _comingSoonMovies

            loadingState.value = true      // Bắt đầu hiển thị loading
            _errorMessage.value = null     // Xóa lỗi cũ trước khi gọi API mới
            try {
                // Gọi hàm suspend trong Repository (phải trong coroutine)
                val movies = if (type == MovieType.NOW_SHOWING) {
                    repository.getNowShowingMovies()
                } else {
                    repository.getComingSoonMovies()
                }
                // Cập nhật giá trị cho StateFlow, UI sẽ tự động nhận biết
                movieState.value = movies
            } catch (e: Exception) { // Bắt lỗi (vd: lỗi mạng, lỗi server)
                // Cập nhật StateFlow lỗi
                _errorMessage.value = "Không thể tải danh sách phim: ${e.localizedMessage}" // Lấy thông báo lỗi
                movieState.value = emptyList() // Có thể xóa danh sách cũ khi lỗi
                // In lỗi ra Logcat để debug
                Log.e("HomeViewModel", "Error loading movies", e)
            } finally {
                // Dù thành công hay lỗi, cũng phải dừng loading
                loadingState.value = false
            }
        }
    }
}