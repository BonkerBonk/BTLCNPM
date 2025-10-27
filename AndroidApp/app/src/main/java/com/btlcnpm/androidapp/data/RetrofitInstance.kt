package com.btlcnpm.androidapp.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// object tạo ra một singleton (chỉ có 1 instance duy nhất)
object RetrofitInstance {

    // IP 10.0.2.2 là địa chỉ đặc biệt để máy ảo Android
    // kết nối đến localhost của máy tính đang chạy nó.
    // Cổng 8080 là cổng ApiGateway của bạn.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Tạo OkHttpClient để có thể tùy chỉnh (vd: thêm timeout, interceptor)
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor (AuthInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS) // Thời gian chờ tối đa để kết nối
        .readTimeout(30, TimeUnit.SECONDS)    // Thời gian chờ tối đa để đọc dữ liệu
        .writeTimeout(30, TimeUnit.SECONDS)   // Thời gian chờ tối đa để ghi dữ liệu
        // .addInterceptor(AuthInterceptor()) // Sau này thêm Interceptor để gửi token tại đây
        .build()

    // Tạo đối tượng ApiService bằng lazy delegate
    // Retrofit chỉ được khởi tạo lần đầu tiên khi api được gọi
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Đặt URL gốc
            .client(okHttpClient) // Sử dụng OkHttpClient tùy chỉnh
            .addConverterFactory(GsonConverterFactory.create()) // Chọn Gson để phân tích JSON
            .build()
            .create(ApiService::class.java) // Tạo implementation cho interface ApiService
    }
}