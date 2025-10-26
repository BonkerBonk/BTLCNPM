package com.btlcnpm.androidapp.ui.screens.auth // Thay package của bạn

import android.util.Log // Import Log để ghi log lỗi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Coroutine scope dành riêng cho ViewModel
import com.btlcnpm.androidapp.data.AuthRepository
import com.btlcnpm.androidapp.data.LoginRequest
import com.btlcnpm.androidapp.data.TokenManager // Lớp quản lý token (sẽ tạo ở bước 7)
import kotlinx.coroutines.flow.MutableStateFlow // StateFlow có thể thay đổi giá trị
import kotlinx.coroutines.flow.StateFlow         // StateFlow chỉ đọc
import kotlinx.coroutines.flow.asStateFlow       // Chuyển đổi
import kotlinx.coroutines.launch                 // Chạy coroutine

// Sử dụng sealed class để định nghĩa các trạng thái có thể có của màn hình Login
// Giúp xử lý các trạng thái trong UI một cách tường minh và an toàn
sealed class LoginState {
    object Idle : LoginState()      // Trạng thái ban đầu, chưa làm gì
    object Loading : LoginState()   // Đang trong quá trình gọi API
    object Success : LoginState()   // Đăng nhập thành công
    // Trạng thái lỗi, chứa thông báo lỗi
    data class Error(val message: String) : LoginState()
}

// ViewModel kế thừa từ androidx.lifecycle.ViewModel
class AuthViewModel(
    // Tạm thời tạo Repository trực tiếp, nên dùng DI sau này
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    // _loginState: MutableStateFlow, chỉ có ViewModel được phép thay đổi giá trị này
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle) // Giá trị khởi tạo là Idle
    // loginState: StateFlow công khai, Composable sẽ lắng nghe state này
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // Hàm này sẽ được gọi từ Composable (LoginScreen) khi người dùng nhấn nút Đăng nhập
    fun login(email: String, password: String) {
        // Kiểm tra nếu đang loading thì không cho gọi API nữa, tránh spam click
        if (_loginState.value is LoginState.Loading) {
            Log.d("AuthViewModel", "Login request ignored, already loading.")
            return
        }

        Log.d("AuthViewModel", "Attempting login for email: $email")
        // Chạy coroutine trong scope của ViewModel
        viewModelScope.launch {
            // 1. Cập nhật trạng thái sang Loading -> UI sẽ hiển thị vòng quay
            _loginState.value = LoginState.Loading
            try {
                // 2. Tạo đối tượng request từ email, password
                val request = LoginRequest(email = email, password = password)
                // 3. Gọi hàm login trong Repository (đây là network call, phải trong coroutine)
                Log.d("AuthViewModel", "Calling authRepository.login...")
                val response = authRepository.login(request)
                Log.d("AuthViewModel", "Login API call successful. Token received.")

                // 4. LƯU TOKEN LẠI - Bước cực kỳ quan trọng!
                TokenManager.saveToken(response.token)
                Log.i("AuthViewModel", "Token saved successfully.")

                // 5. Cập nhật trạng thái sang Success -> UI sẽ xử lý (vd: điều hướng)
                _loginState.value = LoginState.Success

            } catch (e: Exception) { // Bắt tất cả các loại lỗi (mạng, server trả lỗi 4xx/5xx, parse JSON lỗi,...)
                Log.e("AuthViewModel", "Login failed", e)
                // 6. Cập nhật trạng thái sang Error, kèm thông báo lỗi -> UI sẽ hiển thị Toast/Snackbar
                // e.localizedMessage thường chứa thông báo lỗi dễ hiểu hơn
                _loginState.value = LoginState.Error(e.localizedMessage ?: "Đã xảy ra lỗi không mong muốn")
            }
        }
    }

    // Hàm này được gọi từ UI sau khi đã xử lý trạng thái Success hoặc Error
    // để đưa UI về trạng thái chờ nhập liệu tiếp
    fun resetState() {
        // Chỉ reset nếu đang ở Success hoặc Error
        if (_loginState.value is LoginState.Success || _loginState.value is LoginState.Error) {
            Log.d("AuthViewModel", "Resetting login state to Idle.")
            _loginState.value = LoginState.Idle
        }
    }
}