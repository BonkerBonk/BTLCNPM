package com.btlcnpm.androidapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.btlcnpm.androidapp.navigation.AppNavHost
import com.btlcnpm.androidapp.ui.screens.AuthViewModel
import com.btlcnpm.androidapp.ui.screens.BookingViewModel
import com.btlcnpm.androidapp.ui.screens.MovieViewModel
import com.btlcnpm.androidapp.ui.screens.TheaterViewModel // <<< THÊM IMPORT
import com.btlcnpm.androidapp.ui.theme.AndroidAppTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Sử dụng AndroidAppTheme đã tạo
            AndroidAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScreen() // Gọi Composable chính
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val movieViewModel: MovieViewModel = viewModel(factory = MovieViewModel.Factory)
    val theaterViewModel: TheaterViewModel = viewModel(factory = TheaterViewModel.Factory)
    val bookingViewModel: BookingViewModel = viewModel(factory = BookingViewModel.Factory)

    AppNavHost(
        navController = navController,
        authViewModel = authViewModel,
        movieViewModel = movieViewModel,
        theaterViewModel = theaterViewModel // <<< TRUYỀN VÀO
    )
}