package com.btlcnpm.androidapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Import đúng
import com.btlcnpm.androidapp.data.model.LoginRequest

@Composable
fun LoginScreen(
    // Sử dụng Factory để lấy ViewModel
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    // Callback để thông báo đăng nhập thành công và chuyển màn hình
    onLoginSuccessNavigation: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val authState by authViewModel.authUiState.collectAsState()

    var email by remember { mutableStateOf("") } // Dữ liệu test
    var password by remember { mutableStateOf("") } // Dữ liệu test
    var errorMessage by remember { mutableStateOf<String?>(null) } // Lưu trữ thông báo lỗi

    // Lắng nghe sự thay đổi của authState
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.LoginSuccess -> {
                errorMessage = null // Xóa lỗi nếu thành công
                onLoginSuccessNavigation() // Gọi callback để chuyển màn hình
            }
            is AuthUiState.Error -> {
                errorMessage = state.message // Hiển thị lỗi
            }
            is AuthUiState.Initial, AuthUiState.Loading -> {
                errorMessage = null
            }
            else -> {
                errorMessage = null // Xóa lỗi ở các trạng thái khác
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp), // Tăng padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Đăng Nhập", style = MaterialTheme.typography.headlineMedium) // Tiêu đề lớn hơn
        Spacer(Modifier.height(40.dp)) // Khoảng cách lớn hơn

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage != null // Hiển thị lỗi nếu có
        )
        Spacer(Modifier.height(16.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(), // Ẩn mật khẩu
            isError = errorMessage != null // Hiển thị lỗi nếu có
        )
        Spacer(Modifier.height(24.dp))

        // Nút Login
        Button(
            onClick = {
                errorMessage = null // Xóa lỗi cũ trước khi gọi API
                authViewModel.login(LoginRequest(email = email.trim(), password = password))
            },
            // Vô hiệu hóa nút khi đang loading
            enabled = authState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth().height(48.dp) // Kích thước nút chuẩn
        ) {
            // Hiển thị text hoặc loading indicator
            if (authState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary // Màu trắng trên nền nút
                )
            } else {
                Text("Đăng Nhập")
            }
        }

        // Hiển thị thông báo lỗi (nếu có)
        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // TODO: Thêm nút/link cho Đăng ký và Quên mật khẩu
        Spacer(Modifier.height(16.dp))
        TextButton(onClick =  onNavigateToRegister ) {
            Text("Chưa có tài khoản? Đăng ký ngay")
        }
        TextButton(onClick =  onNavigateToForgotPassword ) {
            Text("Quên mật khẩu?")
        }
    }
}