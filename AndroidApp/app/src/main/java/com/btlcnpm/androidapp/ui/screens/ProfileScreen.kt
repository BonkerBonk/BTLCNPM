package com.btlcnpm.androidapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.btlcnpm.androidapp.data.model.FirestoreTimestamp
import com.btlcnpm.androidapp.data.model.Ticket
import com.btlcnpm.androidapp.data.model.UpdateProfileRequest
import com.btlcnpm.androidapp.navigation.Screen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    onLogoutNavigation: () -> Unit,
    navController: NavController // Callback để quay lại màn Login
) {
    val profileState by authViewModel.profileUiState.collectAsState()
    val ticketState by authViewModel.ticketUiState.collectAsState() // <<< LẤY STATE CỦA VÉ
    val authState by authViewModel.authUiState.collectAsState()

    // State cho Tab (0 = Thông tin, 1 = Vé của tôi)
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Lấy profile khi màn hình được hiển thị lần đầu
    LaunchedEffect(key1 = authState) {
        if (authState is AuthUiState.LoginSuccess) {
            if (profileState is ProfileUiState.Idle) {
                authViewModel.fetchProfile() // Hàm này giờ đã tự động gọi fetchMyTickets()
            }
        }
    }

    // Giao diện chính
    Column(modifier = Modifier.fillMaxSize()) {

        // 1. Tab Row
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Thông Tin") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Vé Của Tôi") }
            )
        }

        // 2. Nội dung Tab
        when (selectedTabIndex) {
            0 -> ProfileInfoTab( // Tab 0: Hiển thị thông tin
                profileState = profileState,
                authViewModel = authViewModel,
                onLogoutNavigation = onLogoutNavigation
            )
            1 -> MyTicketsTab( // Tab 1: Hiển thị vé
                ticketState = ticketState,
                onRefresh = { authViewModel.fetchMyTickets() },
                navController = navController
            )
        }
    }
}

// --- TAB THÔNG TIN CÁ NHÂN (Code cũ của bạn) ---
@Composable
fun ProfileInfoTab(
    profileState: ProfileUiState,
    authViewModel: AuthViewModel,
    onLogoutNavigation: () -> Unit
) {
    // State cho các trường input
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var updateErrorMessage by remember { mutableStateOf<String?>(null) }
    var updateSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Cập nhật state của các TextField khi profileState thay đổi
    LaunchedEffect(profileState) {
        if (profileState is ProfileUiState.Success) {
            val profile = (profileState as ProfileUiState.Success).profile
            fullName = profile.fullName ?: ""
            phoneNumber = profile.phoneNumber ?: ""
            dateOfBirth = profile.dateOfBirth ?: ""
            updateErrorMessage = null
            if (profileState !is ProfileUiState.Updating) {
                // (Không cần hiển thị success message)
            }
        } else if (profileState is ProfileUiState.Error && profileState !is ProfileUiState.Updating) {
            updateErrorMessage = (profileState as ProfileUiState.Error).message
        }
    }

    // Đây là code Column cũ trong file ProfileScreen.kt của bạn
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Thông Tin Cá Nhân", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        when (val state = profileState) {
            ProfileUiState.Idle -> Text("Vui lòng đăng nhập để xem thông tin.")
            ProfileUiState.Loading -> CircularProgressIndicator()
            is ProfileUiState.Error -> {
                if (updateErrorMessage == null) {
                    Text("Lỗi: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = { authViewModel.fetchProfile() }) {
                    Text("Thử lại")
                }
            }
            is ProfileUiState.Success, ProfileUiState.Updating -> {
                val profile = (state as? ProfileUiState.Success)?.profile

                profile?.email?.let {
                    OutlinedTextField(
                        value = it,
                        onValueChange = {},
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Họ và Tên") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = updateErrorMessage != null
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = updateErrorMessage != null
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text("Ngày sinh (YYYY-MM-DD)") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = updateErrorMessage != null
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        updateErrorMessage = null
                        updateSuccessMessage = null
                        val request = UpdateProfileRequest(
                            fullName = fullName.trim().takeIf { it.isNotEmpty() },
                            phoneNumber = phoneNumber.trim().takeIf { it.isNotEmpty() },
                            dateOfBirth = dateOfBirth.trim().takeIf { it.isNotEmpty() }
                        )
                        authViewModel.updateProfile(request)
                    },
                    enabled = state !is ProfileUiState.Updating,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (state is ProfileUiState.Updating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Cập Nhật Thông Tin")
                    }
                }

                if (updateErrorMessage != null && state !is ProfileUiState.Loading) {
                    Spacer(Modifier.height(8.dp))
                    Text(updateErrorMessage!!, color = MaterialTheme.colorScheme.error)
                }
                if (updateSuccessMessage != null && state is ProfileUiState.Success && state !is ProfileUiState.Updating) {
                    Spacer(Modifier.height(8.dp))
                    Text(updateSuccessMessage!!, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.height(40.dp))
                OutlinedButton(
                    onClick = {
                        authViewModel.logout()
                        onLogoutNavigation()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Đăng Xuất")
                }
            }
        }
    }
}

// --- COMPOSABLE MỚI CHO TAB VÉ CỦA TÔI ---
@Composable
fun MyTicketsTab(
    ticketState: TicketUiState,
    onRefresh: () -> Unit,
    navController: NavController
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (ticketState) {
            is TicketUiState.Idle, is TicketUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is TicketUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Lỗi: ${ticketState.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRefresh) { Text("Thử Lại") }
                }
            }
            is TicketUiState.Success -> {
                // Backend đã lọc vé "VALID", ta chỉ cần hiển thị
                if (ticketState.tickets.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Bạn chưa có vé nào còn hạn sử dụng.")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ticketState.tickets) { ticket ->
                            TicketItem(ticket = ticket, onTicketClick = { ticketId ->
                                navController.navigate(Screen.TicketQR.createRoute(ticketId))
                            })
                        }
                    }
                }
            }
        }
    }
}

// --- COMPOSABLE MỚI CHO 1 ITEM VÉ ---
@Composable
fun TicketItem(
    ticket: Ticket,
    onTicketClick: (String) -> Unit
) {
    // Hàm helper để đổi Timestamp sang String (dd/MM/yyyy HH:mm)
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTicketTime(timestamp: FirestoreTimestamp?): String {
        if (timestamp?.seconds == null) return "N/A"
        try {
            val instant = Instant.ofEpochSecond(timestamp.seconds)
            val formatter = DateTimeFormatter
                .ofPattern("HH:mm - dd/MM/yyyy") // Định dạng Giờ:Phút - Ngày/Tháng/Năm
                .withZone(ZoneId.systemDefault())
            return formatter.format(instant)
        } catch (e: Exception) {
            return "Invalid Date"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onTicketClick(ticket.ticketId) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = ticket.movieTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Rạp: ${ticket.theaterName} - ${ticket.roomName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Suất: ${formatTicketTime(ticket.showtimeStartTime)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))

            // Badge trạng thái
            val statusColor = when (ticket.status) {
                "VALID" -> MaterialTheme.colorScheme.primary
                "USED" -> MaterialTheme.colorScheme.error
                "EXPIRED" -> MaterialTheme.colorScheme.outline
                else -> MaterialTheme.colorScheme.onSurface
            }

            Text(
                text = ticket.status,
                color = statusColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}