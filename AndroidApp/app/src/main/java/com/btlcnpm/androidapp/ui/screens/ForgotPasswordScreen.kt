package com.btlcnpm.androidapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    onNavigateBack: () -> Unit // Callback để quay lại màn hình trước (Login)
) {
    val forgotPasswordState by authViewModel.forgotPasswordUiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<Pair<String, Boolean>?>(null) } // Pair<Nội dung, Là lỗi hay thành công?>

    // Reset state khi rời màn hình (tùy chọn)
    DisposableEffect(Unit) {
        onDispose {
            authViewModel.resetForgotPasswordState()
        }
    }

    // Lắng nghe state
    LaunchedEffect(forgotPasswordState) {
        when (val state = forgotPasswordState) {
            is ForgotPasswordUiState.Success -> {
                message = Pair(state.message, false) // false = không phải lỗi
            }
            is ForgotPasswordUiState.Error -> {
                message = Pair(state.message, true) // true = là lỗi
            }
            else -> {
                message = null // Xóa message khi Idle hoặc Loading
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quên Mật Khẩu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                "Nhập email của bạn để nhận liên kết đặt lại mật khẩu.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = message?.second == true // Hiển thị lỗi nếu có
            )
            Spacer(Modifier.height(24.dp))

            // Nút Gửi
            Button(
                onClick = {
                    message = null // Xóa message cũ
                    authViewModel.sendPasswordReset(email.trim())
                },
                enabled = forgotPasswordState !is ForgotPasswordUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (forgotPasswordState is ForgotPasswordUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Gửi Liên Kết Đặt Lại")
                }
            }

            // Hiển thị thông báo thành công hoặc lỗi
            if (message != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = message!!.first,
                    color = if (message!!.second) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, // Đỏ nếu lỗi, xanh nếu thành công
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                // Thêm nút OK hoặc tự ẩn sau vài giây nếu muốn
            }
        }
    }
}