package com.btlcnpm.androidapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.btlcnpm.androidapp.data.model.RegisterRequest

@OptIn(ExperimentalMaterial3Api::class) // Cho TopAppBar
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    onRegisterSuccessNavigation: () -> Unit, // Callback khi đăng ký thành công
    onNavigateBack: () -> Unit // Callback để quay lại màn hình trước (Login)
) {
    val authState by authViewModel.authUiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") } // Thêm field xác nhận mật khẩu
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordMismatchError by remember { mutableStateOf(false) } // State báo lỗi mật khẩu không khớp

    // Xử lý khi trạng thái auth thay đổi
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.RegisterSuccess -> {
                errorMessage = null
                onRegisterSuccessNavigation() // Chuyển màn hình khi thành công
            }
            is AuthUiState.Error -> {
                errorMessage = state.message // Hiển thị lỗi từ ViewModel
            }
            is AuthUiState.Initial, AuthUiState.Loading -> {
                errorMessage = null
            }
            else -> {
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đăng Ký Tài Khoản") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Nút back
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Áp dụng padding từ Scaffold
                .padding(horizontal = 24.dp, vertical = 20.dp), // Thêm padding riêng
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Full Name field
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Họ và Tên") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )
            Spacer(Modifier.height(16.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )
            Spacer(Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    // Kiểm tra khớp mật khẩu ngay khi thay đổi
                    passwordMismatchError = confirmPassword.isNotEmpty() && it != confirmPassword
                },
                label = { Text("Mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = errorMessage != null || passwordMismatchError // Lỗi nếu API lỗi HOẶC không khớp
            )
            Spacer(Modifier.height(16.dp))

            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    // Kiểm tra khớp mật khẩu ngay khi thay đổi
                    passwordMismatchError = password.isNotEmpty() && it != password
                },
                label = { Text("Xác nhận mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordMismatchError // Chỉ báo lỗi không khớp ở đây
            )
            // Hiển thị thông báo nếu mật khẩu không khớp
            if (passwordMismatchError) {
                Text(
                    "Mật khẩu không khớp.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            Spacer(Modifier.height(24.dp))

            // Nút Register
            Button(
                onClick = {
                    errorMessage = null // Xóa lỗi API cũ
                    // Kiểm tra mật khẩu khớp trước khi gọi API
                    if (password == confirmPassword && password.isNotEmpty()) {
                        passwordMismatchError = false // Đảm bảo lỗi khớp được xóa
                        authViewModel.register(
                            RegisterRequest(
                                email = email.trim(),
                                password = password, // Không trim mật khẩu
                                fullName = fullName.trim()
                            )
                        )
                    } else if (password != confirmPassword) {
                        passwordMismatchError = true // Hiển thị lỗi không khớp
                    } else {
                        // Có thể thêm báo lỗi nếu các trường khác trống
                        errorMessage = "Vui lòng điền đầy đủ thông tin."
                    }
                },
                enabled = authState !is AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (authState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Đăng Ký")
                }
            }

            // Hiển thị thông báo lỗi từ API
            if (errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}