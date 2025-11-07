package com.btlcnpm.androidapp.data.remote

// Thêm các import repository
import com.btlcnpm.androidapp.data.repository.AuthRepository
import com.btlcnpm.androidapp.data.repository.BookingRepository
import com.btlcnpm.androidapp.data.repository.MovieRepository
import com.btlcnpm.androidapp.data.repository.TheaterRepository
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {

    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder().setLenient().create()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: BetaCinemaApi by lazy {
        retrofit.create(BetaCinemaApi::class.java)
    }

    // === QUẢN LÝ CÁC REPOSITORY SINGLETON ===

    // 1. AuthRepository (Singleton)
    // (authRepository phải được khai báo trước BookingRepository)
    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService)
    }

    // 2. MovieRepository (Singleton)
    val movieRepository: MovieRepository by lazy {
        MovieRepository(apiService)
    }

    // 3. TheaterRepository (Singleton)
    val theaterRepository: TheaterRepository by lazy {
        TheaterRepository(apiService)
    }

    // 4. BookingRepository (Singleton)
    val bookingRepository: BookingRepository by lazy {
        // Truyền AuthRepository (Singleton) vào
        BookingRepository(apiService, authRepository)
    }
}