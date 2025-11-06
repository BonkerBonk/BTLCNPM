package com.btlcnpm.androidapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.btlcnpm.androidapp.data.model.LoginRequest
import com.btlcnpm.androidapp.data.model.RegisterRequest
import com.btlcnpm.androidapp.data.model.AuthResponse
import com.btlcnpm.androidapp.data.model.Ticket
import com.btlcnpm.androidapp.data.model.UserProfile
import com.btlcnpm.androidapp.data.model.UpdateProfileRequest
import com.btlcnpm.androidapp.data.remote.ApiConfig
import com.btlcnpm.androidapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Trạng thái cho quá trình Authentication (Login/Register)
sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    // Tách Success thành 2 trạng thái riêng biệt:
    data class LoginSuccess(val response: AuthResponse) : AuthUiState() // Khi đăng nhập thành công
    object RegisterSuccess : AuthUiState() // Khi đăng ký thành công (không có data)
    data class Error(val message: String) : AuthUiState()
}

// Trạng thái cho dữ liệu Profile
sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    object Updating: ProfileUiState() // Đang cập nhật
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()      // Trạng thái ban đầu
    object Loading : ForgotPasswordUiState()   // Đang gửi yêu cầu
    data class Success(val message: String) : ForgotPasswordUiState() // Gửi thành công
    data class Error(val message: String) : ForgotPasswordUiState()   // Gửi thất bại
}

sealed class TicketUiState {
    object Idle : TicketUiState()
    object Loading : TicketUiState()
    data class Success(val tickets: List<Ticket>) : TicketUiState()
    data class Error(val message: String) : TicketUiState()
}

// ViewModel quản lý trạng thái và logic cho Auth & Profile
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // StateFlow cho trạng thái Authentication
    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial) // Dùng Initial
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // StateFlow cho trạng thái Profile
    private val _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    private val _forgotPasswordUiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val forgotPasswordUiState: StateFlow<ForgotPasswordUiState> = _forgotPasswordUiState.asStateFlow()

    // THÊM STATE FLOW MỚI CHO VÉ
    private val _ticketUiState = MutableStateFlow<TicketUiState>(TicketUiState.Idle)
    val ticketUiState: StateFlow<TicketUiState> = _ticketUiState.asStateFlow()

    // Hàm xử lý Login
    fun login(request: LoginRequest) {
        _authUiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.login(request)
            result.onSuccess {
                // Sửa: Đặt trạng thái là LoginSuccess
                _authUiState.value = AuthUiState.LoginSuccess(it)
                fetchProfile()
            }.onFailure {
                _authUiState.value = AuthUiState.Error(parseErrorMessage(it))
            }
        }
    }

    // Hàm xử lý Register
    fun register(request: RegisterRequest) {
        _authUiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.register(request)
            result.onSuccess {
                // Sửa: Đặt trạng thái là RegisterSuccess
                _authUiState.value = AuthUiState.RegisterSuccess
            }.onFailure {
                _authUiState.value = AuthUiState.Error(parseErrorMessage(it))
            }
        }
    }


    // Hàm lấy thông tin Profile
    fun fetchProfile() {
        // Chỉ fetch nếu đã login (có token)
        if (authRepository.authToken == null && _authUiState.value !is AuthUiState.LoginSuccess) {
            _profileUiState.value = ProfileUiState.Error("Please log in first.")
            return
        }

        _profileUiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            val result = authRepository.getMyProfile()
            result.onSuccess {
                _profileUiState.value = ProfileUiState.Success(it)
            }.onFailure {
                _profileUiState.value = ProfileUiState.Error(parseErrorMessage(it))
                // Nếu lỗi (vd: token hết hạn), set lại trạng thái auth về Idle/Error
                if (it is HttpException && it.code() == 401) { // Unauthorized
                    _authUiState.value = AuthUiState.Error("Session expired. Please log in again.")
                    authRepository.logout() // Xóa token cũ
                }
            }
        }
        fetchMyTickets()
    }

    // Hàm cập nhật Profile
    fun updateProfile(request: UpdateProfileRequest) {
        if (authRepository.authToken == null) {
            _profileUiState.value = ProfileUiState.Error("Please log in first.")
            return
        }

        _profileUiState.value = ProfileUiState.Updating // Đặt trạng thái đang cập nhật
        viewModelScope.launch {
            val result = authRepository.updateMyProfile(request)
            result.onSuccess {
                _profileUiState.value = ProfileUiState.Success(it) // Cập nhật thành công, hiển thị profile mới
            }.onFailure {
                // Giữ lại profile cũ trên UI nhưng báo lỗi
                val currentProfile = (_profileUiState.value as? ProfileUiState.Success)?.profile
                _profileUiState.value = ProfileUiState.Error(parseErrorMessage(it))
                // Nếu vẫn có profile cũ, có thể set lại Success để UI không bị trống
                if (currentProfile != null) {
                    _profileUiState.value = ProfileUiState.Success(currentProfile) // Tạm thời quay lại state cũ
                    // TODO: Hiển thị Snackbar hoặc thông báo lỗi riêng biệt
                }
            }
        }
    }

    // Hàm đăng xuất
    fun logout() {
        authRepository.logout()
        _authUiState.value = AuthUiState.Initial
        _profileUiState.value = ProfileUiState.Idle // Reset profile state
        _ticketUiState.value = TicketUiState.Idle
    }

    fun sendPasswordReset(email: String) {
        _forgotPasswordUiState.value = ForgotPasswordUiState.Loading
        viewModelScope.launch {
            val result = authRepository.forgotPassword(email)
            result.onSuccess { message ->
                _forgotPasswordUiState.value = ForgotPasswordUiState.Success(message)
            }.onFailure { error ->
                _forgotPasswordUiState.value = ForgotPasswordUiState.Error(parseErrorMessage(error))
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordUiState.value = ForgotPasswordUiState.Idle
    }

    // Trong AuthViewModel.kt
// Thêm hàm này để reset trạng thái
    fun resetUiState() {
        _authUiState.value = AuthUiState.Initial // Đảm bảo reset về Initial
    }

    fun resetAuthErrorState() { // Đổi tên hàm này cho rõ nghĩa
        _authUiState.value = AuthUiState.Initial
    }

    // HÀM MỚI: Lấy vé
    fun fetchMyTickets() {
        _ticketUiState.value = TicketUiState.Loading
        viewModelScope.launch {
            val result = authRepository.getMyTickets()
            result.onSuccess {
                _ticketUiState.value = TicketUiState.Success(it)
            }.onFailure {
                _ticketUiState.value = TicketUiState.Error(parseErrorMessage(it))
            }
        }
    }

    // Hàm helper để phân tích lỗi
    private fun parseErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                // === LOGIC MỚI: BẮT LỖI 401 ===
                if (throwable.code() == 401) {
                    // Kiểm tra nếu là lỗi 401, trả về thông báo thân thiện
                    return "Email hoặc mật khẩu không chính xác."
                }
                // === LOGIC CŨ CHO CÁC LỖI HTTP KHÁC ===
                val errorBody = throwable.response()?.errorBody()?.string()
                "Error ${throwable.code()}: ${throwable.message()} ${errorBody?.take(100) ?: ""}"
            }
            is IOException -> "Network error. Please check your connection."
            else -> throwable.localizedMessage ?: "An unexpected error occurred."
        }
    }


    // ViewModel Factory để khởi tạo ViewModel thủ công (không dùng Hilt)
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // SỬA LẠI: Lấy repository từ ApiConfig
                val repository = ApiConfig.authRepository
                return AuthViewModel(repository) as T
            }
        }
    }
}