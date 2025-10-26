package com.btlcnpm.androidapp.ui.screens.auth // Thay package của bạn

import android.widget.Toast // Dùng tạm để hiển thị thông báo
import androidx.compose.foundation.layout.* // Column, Row, Spacer, padding, fillMaxSize, fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions // Để cấu hình bàn phím (vd: email, password)
import androidx.compose.material3.* // Button, OutlinedTextField, Text, CircularProgressIndicator, MaterialTheme
import androidx.compose.runtime.* // Composable, remember, mutableStateOf, collectAsState, LaunchedEffect, getValue, setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Để lấy Context hiển thị Toast
import androidx.compose.ui.text.input.KeyboardType // Loại bàn phím
import androidx.compose.ui.text.input.PasswordVisualTransformation // Để ẩn mật khẩu (*)
import androidx.compose.ui.tooling.preview.Preview // Để xem trước giao diện
import androidx.compose.ui.unit.dp // Đơn vị dp
import androidx.lifecycle.viewmodel.compose.viewModel // Hàm để lấy ViewModel

@Composable
fun LoginScreen(
    // Callback này sẽ được gọi khi đăng nhập thành công để điều hướng
    onLoginSuccess: () -> Unit,
    // (Tùy chọn) Callback để điều hướng đến màn hình Đăng ký
    // onNavigateToRegister: () -> Unit,
    // Lấy instance của AuthViewModel, Compose sẽ tự quản lý vòng đời
    authViewModel: AuthViewModel = viewModel()
) {
    // Sử dụng remember và mutableStateOf để lưu trạng thái của các ô nhập liệu
    // Giao diện sẽ tự cập nhật khi các biến này thay đổi
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Lắng nghe loginState từ ViewModel bằng collectAsState
    // Khi loginState thay đổi, Composable này sẽ được vẽ lại (recompose)
    val loginState by authViewModel.loginState.collectAsState()
    // Lấy Context hiện tại để dùng cho Toast
    val context = LocalContext.current

    // LaunchedEffect dùng để chạy code (side effect) khi một state thay đổi
    // Ở đây, chúng ta dùng nó để hiển thị Toast và điều hướng khi loginState là Success hoặc Error
    LaunchedEffect(loginState) { // key = loginState -> chỉ chạy lại khi loginState thay đổi
        when (val state = loginState) {
            is LoginState.Success -> {
                // Hiển thị thông báo thành công
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                // Gọi callback để AppNavigation thực hiện điều hướng
                onLoginSuccess()
                // Có thể reset state ngay tại đây hoặc để AppNavigation xử lý
                // authViewModel.resetState() // Không nên reset ngay lập tức nếu cần giữ state Success để kiểm tra
            }
            is LoginState.Error -> {
                // Hiển thị thông báo lỗi chi tiết từ ViewModel
                Toast.makeText(context, "Lỗi đăng nhập: ${state.message}", Toast.LENGTH_LONG).show()
                // Reset trạng thái trong ViewModel để nút bấm hoạt động lại
                authViewModel.resetState()
            }
            // Không cần làm gì với trạng thái Idle hoặc Loading ở đây
            else -> { /* No side effects needed for Idle or Loading */ }
        }
    }

    // --- Bố cục Giao diện ---
    // Column xếp các thành phần con theo chiều dọc
    Column(
        modifier = Modifier
            .fillMaxSize() // Chiếm toàn bộ không gian màn hình
            .padding(horizontal = 24.dp), // Thêm padding trái phải
        // Căn giữa các thành phần con theo chiều dọc
        verticalArrangement = Arrangement.Center,
        // Căn giữa các thành phần con theo chiều ngang
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tiêu đề màn hình
        Text(
            text = "Đăng nhập",
            style = MaterialTheme.typography.headlineLarge // Style chữ lớn từ Theme
        )
        // Khoảng trống giữa tiêu đề và ô nhập liệu
        Spacer(modifier = Modifier.height(48.dp))

        // Ô nhập liệu cho Email
        OutlinedTextField(
            value = email, // Giá trị hiện tại của ô nhập
            // Hàm được gọi mỗi khi người dùng gõ chữ, cập nhật state 'email'
            onValueChange = { email = it },
            label = { Text("Email") }, // Nhãn gợi ý
            modifier = Modifier.fillMaxWidth(), // Chiếm hết chiều ngang
            // Cấu hình bàn phím hiển thị dạng email
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true // Chỉ cho phép nhập trên một dòng
        )
        // Khoảng trống
        Spacer(modifier = Modifier.height(16.dp))

        // Ô nhập liệu cho Mật khẩu
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            // Ẩn các ký tự nhập vào bằng dấu '*'
            visualTransformation = PasswordVisualTransformation(),
            // Cấu hình bàn phím hiển thị dạng mật khẩu
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        // Khoảng trống
        Spacer(modifier = Modifier.height(32.dp))

        // Nút Đăng nhập
        Button(
            onClick = {
                // Khi nhấn nút, gọi hàm login trong ViewModel với email và password đã nhập
                // trim() để loại bỏ khoảng trắng thừa ở đầu và cuối
                authViewModel.login(email.trim(), password)
            },
            modifier = Modifier
                .fillMaxWidth() // Chiếm hết chiều ngang
                .height(50.dp), // Chiều cao cố định cho nút
            // Nút sẽ bị vô hiệu hóa (không nhấn được) khi trạng thái là Loading
            enabled = loginState !is LoginState.Loading
        ) {
            // Kiểm tra trạng thái để hiển thị chữ hoặc vòng quay loading
            if (loginState is LoginState.Loading) {
                // Hiển thị vòng quay loading nhỏ bên trong nút
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), // Kích thước vòng quay
                    color = MaterialTheme.colorScheme.onPrimary // Màu của vòng quay (thường là trắng)
                )
            } else {
                // Hiển thị chữ "Đăng nhập" khi không loading
                Text("Đăng nhập")
            }
        }

        // (Tùy chọn) Thêm nút/link cho chức năng Quên mật khẩu hoặc Đăng ký
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { /* TODO: Gọi onNavigateToRegister() */ }) {
            Text("Chưa có tài khoản? Đăng ký ngay")
        }
    }
}

// Hàm Preview giúp xem trước giao diện ngay trong Android Studio (không cần chạy app)
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    // Cần bọc Preview trong Theme để thấy đúng style
    // com.btlcnpm.androidapp.ui.theme.BTLCNPMTheme { // Thay bằng tên Theme của bạn
    LoginScreen(onLoginSuccess = {}) // Cung cấp lambda rỗng cho callback
    // }
}