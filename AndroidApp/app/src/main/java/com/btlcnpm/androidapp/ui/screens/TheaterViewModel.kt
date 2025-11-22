package com.btlcnpm.androidapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.btlcnpm.androidapp.data.model.Theater
import com.btlcnpm.androidapp.data.remote.ApiConfig
import com.btlcnpm.androidapp.data.repository.TheaterRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import org.json.JSONObject
// Trạng thái cho danh sách rạp
sealed class TheaterListUiState {
    object Idle : TheaterListUiState()
    object Loading : TheaterListUiState()
    data class Success(val theaters: List<Theater>) : TheaterListUiState()
    data class Error(val message: String) : TheaterListUiState()
}

@OptIn(FlowPreview::class)
class TheaterViewModel(private val theaterRepository: TheaterRepository) : ViewModel() {

    // State cho danh sách rạp
    private val _theaterListUiState = MutableStateFlow<TheaterListUiState>(TheaterListUiState.Idle)
    val theaterListUiState: StateFlow<TheaterListUiState> = _theaterListUiState.asStateFlow()

    // State cho ô tìm kiếm (lọc theo thành phố)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        fetchAllTheaters() // Tải tất cả rạp khi ViewModel được tạo
        observeSearchQuery() // Bắt đầu lắng nghe thay đổi của ô tìm kiếm
    }

    // Lấy tất cả rạp
    fun fetchAllTheaters() {
        _theaterListUiState.value = TheaterListUiState.Loading
        viewModelScope.launch {
            val result = theaterRepository.getAllTheaters()
            result.onSuccess {
                _theaterListUiState.value = TheaterListUiState.Success(it)
            }.onFailure {
                _theaterListUiState.value = TheaterListUiState.Error(parseErrorMessage(it))
            }
        }
    }

    // Lấy rạp theo thành phố
    private fun fetchTheatersByCity(city: String) {
        _theaterListUiState.value = TheaterListUiState.Loading
        viewModelScope.launch {
            val result = theaterRepository.getTheatersByCity(city)
            result.onSuccess {
                _theaterListUiState.value = TheaterListUiState.Success(it)
            }.onFailure {
                _theaterListUiState.value = TheaterListUiState.Error(parseErrorMessage(it))
            }
        }
    }

    // Cập nhật query từ TextField
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // Nếu query rỗng, tải lại tất cả
        if (query.trim().isEmpty()) {
            fetchAllTheaters()
        }
    }

    // Lắng nghe thay đổi query để gọi API (sau 500ms)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(500L) // Chờ 500ms sau khi người dùng ngừng gõ
                .filter { it.trim().isNotEmpty() } // Chỉ tìm khi không rỗng
                .distinctUntilChanged() // Không tìm lại nếu query giống hệt
                .collect { query ->
                    fetchTheatersByCity(query.trim()) // Gọi API tìm theo thành phố
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

    // ViewModel Factory
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TheaterViewModel::class.java)) {
                    // SỬA LẠI: Lấy repository từ ApiConfig
                    val repository = ApiConfig.theaterRepository
                    return TheaterViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}