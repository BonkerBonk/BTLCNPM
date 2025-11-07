package com.btlcnpm.androidapp.data.repository

import android.util.Log
import com.btlcnpm.androidapp.data.model.Theater
import com.btlcnpm.androidapp.data.remote.BetaCinemaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

// Repository để xử lý các tác vụ liên quan đến Rạp chiếu phim
class TheaterRepository(private val apiService: BetaCinemaApi) {

    // Lấy tất cả rạp chiếu
    suspend fun getAllTheaters(): Result<List<Theater>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllTheaters()
                if (response.isSuccessful && response.body() != null) {
                    Log.d("TheaterRepository", "Fetched ${response.body()?.size ?: 0} theaters.")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Failed to get theaters"
                    Log.e("TheaterRepository", "Get all theaters failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("TheaterRepository", "Network error getting theaters: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("TheaterRepository", "Unexpected error getting theaters: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Lấy rạp chiếu theo Thành phố
    suspend fun getTheatersByCity(city: String): Result<List<Theater>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTheatersByCity(city)
                if (response.isSuccessful && response.body() != null) {
                    Log.d("TheaterRepository", "Fetched ${response.body()?.size ?: 0} theaters for city $city.")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Failed to get theaters by city"
                    Log.e("TheaterRepository", "Get theaters by city failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("TheaterRepository", "Network error getting theaters by city: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("TheaterRepository", "Unexpected error getting theaters by city: ${e.message}")
                Result.failure(e)
            }
        }
    }
}