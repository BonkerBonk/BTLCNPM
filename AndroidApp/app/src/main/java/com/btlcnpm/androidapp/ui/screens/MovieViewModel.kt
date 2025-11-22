package com.btlcnpm.androidapp.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.btlcnpm.androidapp.data.model.CreateReviewRequest
import com.btlcnpm.androidapp.data.model.Movie
import com.btlcnpm.androidapp.data.model.MovieSearchDTO
import com.btlcnpm.androidapp.data.model.Review
import com.btlcnpm.androidapp.data.remote.ApiConfig
import com.btlcnpm.androidapp.data.repository.MovieRepository
// === THÊM CÁC IMPORT CHO DEBOUNCE ===
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
// === KẾT THÚC THÊM ===
import retrofit2.HttpException
import java.io.IOException
import org.json.JSONObject
// Trạng thái cho danh sách phim
sealed class MovieListUiState {
    object Initial : MovieListUiState() // Dùng Initial thay vì Idle để thống nhất
    object Loading : MovieListUiState()
    data class Success(val movies: List<Movie>) : MovieListUiState()
    data class Error(val message: String) : MovieListUiState()
}

sealed class MovieDetailUiState {
    object Initial : MovieDetailUiState()
    object Loading : MovieDetailUiState()
    data class Success(val movie: Movie) : MovieDetailUiState()
    data class Error(val message: String) : MovieDetailUiState()
}

// --- STATE MỚI CHO DANH SÁCH REVIEW ---
sealed class ReviewListUiState {
    object Initial : ReviewListUiState()
    object Loading : ReviewListUiState()
    data class Success(val reviews: List<Review>) : ReviewListUiState()
    data class Error(val message: String) : ReviewListUiState()
}

sealed class MovieSearchUiState {
    object Idle : MovieSearchUiState() // Trạng thái chờ, chưa tìm
    object Loading : MovieSearchUiState()
    data class Success(val results: List<MovieSearchDTO>) : MovieSearchUiState()
    data class Error(val message: String) : MovieSearchUiState()
}

@OptIn(FlowPreview::class)
class MovieViewModel(private val movieRepository: MovieRepository) : ViewModel() {

    private val _movieListUiState = MutableStateFlow<MovieListUiState>(MovieListUiState.Initial)
    val movieListUiState: StateFlow<MovieListUiState> = _movieListUiState.asStateFlow()

    private val _movieDetailUiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Initial)
    val movieDetailUiState: StateFlow<MovieDetailUiState> = _movieDetailUiState.asStateFlow()

    // --- STATE FLOW MỚI CHO REVIEW ---
    private val _reviewListUiState = MutableStateFlow<ReviewListUiState>(ReviewListUiState.Initial)
    val reviewListUiState: StateFlow<ReviewListUiState> = _reviewListUiState.asStateFlow()


    // State cho nội dung ô tìm kiếm
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // State cho kết quả tìm kiếm
    private val _movieSearchUiState = MutableStateFlow<MovieSearchUiState>(MovieSearchUiState.Idle)
    val movieSearchUiState: StateFlow<MovieSearchUiState> = _movieSearchUiState.asStateFlow()

    // --- Tự động gọi observeSearchQuery khi ViewModel được tạo ---
    init {
        observeSearchQuery()
    }

    // Hàm fetch tất cả phim
    fun fetchAllMovies() {
        if (_movieListUiState.value is MovieListUiState.Loading) return // Tránh gọi lại khi đang tải

        _movieListUiState.value = MovieListUiState.Loading
        viewModelScope.launch {
            val result = movieRepository.getAllMovies()
            result.onSuccess {
                _movieListUiState.value = MovieListUiState.Success(it)
            }.onFailure {
                _movieListUiState.value = MovieListUiState.Error(parseErrorMessage(it))
            }
        }
    }

    // Sửa lại hàm này
    fun fetchMovieById(movieId: String) {
        _movieDetailUiState.value = MovieDetailUiState.Loading
        fetchReviewsByMovieId(movieId) // --- GỌI TẢI REVIEW CÙNG LÚC ---

        viewModelScope.launch {
            val result = movieRepository.getMovieById(movieId)
            result.onSuccess {
                _movieDetailUiState.value = MovieDetailUiState.Success(it)
            }.onFailure {
                _movieDetailUiState.value = MovieDetailUiState.Error(parseErrorMessage(it))
            }
        }
    }

    // --- HÀM MỚI ĐỂ TẢI REVIEW ---
    fun fetchReviewsByMovieId(movieId: String) {
        _reviewListUiState.value = ReviewListUiState.Loading
        viewModelScope.launch {
            val result = movieRepository.getReviewsByMovieId(movieId)
            result.onSuccess {
                _reviewListUiState.value = ReviewListUiState.Success(it)
            }.onFailure {
                _reviewListUiState.value = ReviewListUiState.Error(parseErrorMessage(it))
            }
        }
    }

    // --- HÀM MỚI ĐỂ TẠO REVIEW ---
    fun createReview(request: CreateReviewRequest) {
        viewModelScope.launch {
            val result = movieRepository.createReview(request)
            result.onSuccess {
                // Tải lại danh sách review để hiển thị review mới
                fetchReviewsByMovieId(request.movieId)
            }.onFailure {
                // TODO: Hiển thị lỗi tạo review (ví dụ: qua Snackbar)
                // Tạm thời chỉ log lỗi
                Log.e("MovieViewModel", "Failed to create review: ${parseErrorMessage(it)}")
            }
        }
    }

    // Sửa lại hàm này
    fun clearMovieDetail() {
        _movieDetailUiState.value = MovieDetailUiState.Initial
        _reviewListUiState.value = ReviewListUiState.Initial // --- RESET REVIEW STATE ---
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query

        // Tạo một phiên bản đã cắt khoảng trắng để kiểm tra logic
        val safeQuery = query.trim()

        // Nếu người dùng xóa hết chữ, reset trạng thái tìm kiếm về Idle
        if (safeQuery.isBlank()) {
            _movieSearchUiState.value = MovieSearchUiState.Idle
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(500L) // Chờ 500ms sau khi người dùng ngừng gõ
                .distinctUntilChanged()
                .filter { it.trim().length >= 3 } // Chỉ tìm khi có chữ và dài hơn 2 ký tự
                .distinctUntilChanged() // Không tìm lại nếu query giống hệt
                .collect { query ->
                    executeSearch(query) // Gọi API tìm kiếm
                }
        }
    }

    // Hàm gọi Repository để thực thi tìm kiếm
    private fun executeSearch(query: String) {
        _movieSearchUiState.value = MovieSearchUiState.Loading
        viewModelScope.launch {
            val result = movieRepository.searchMovies(query)
            result.onSuccess {
                _movieSearchUiState.value = MovieSearchUiState.Success(it)
            }.onFailure {
                _movieSearchUiState.value = MovieSearchUiState.Error(parseErrorMessage(it))
            }
        }
    }
    // Hàm helper để phân tích lỗi
    // Hàm này sẽ đọc lỗi JSON từ backend
    private fun parseErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                // Ưu tiên 1: Lỗi 401 (Sai mật khẩu) - Dù ở đâu cũng nên có
                if (throwable.code() == 401) {
                    return "Phiên đăng nhập hết hạn hoặc không hợp lệ."
                }

                // Ưu tiên 2: Thử đọc JSON body để lấy "message"
                try {
                    val errorBody = throwable.response()?.errorBody()?.string()
                    if (errorBody != null) {
                        val jsonObject = JSONObject(errorBody)
                        if (jsonObject.has("message")) {
                            return jsonObject.getString("message") // Ví dụ: "Không đủ vé."
                        }
                    }
                } catch (e: Exception) {
                    // Lỗi khi parse JSON, sẽ đi tiếp xuống lỗi chung
                }

                // Ưu tiên 3: Lỗi chung nếu không parse được
                "Error ${throwable.code()}: ${throwable.message()}"
            }
            is IOException -> "Lỗi mạng. Vui lòng kiểm tra kết nối."
            else -> throwable.localizedMessage ?: "Đã xảy ra lỗi không xác định."
        }
    }


    // ViewModel Factory thủ công
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
                    // SỬA LẠI: Lấy repository từ ApiConfig
                    val repository = ApiConfig.movieRepository
                    return MovieViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

