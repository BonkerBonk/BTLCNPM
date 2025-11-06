package com.btlcnpm.androidapp.data.repository

import android.util.Log
import com.btlcnpm.androidapp.data.model.CreateReviewRequest
import com.btlcnpm.androidapp.data.model.Movie
import com.btlcnpm.androidapp.data.model.MovieSearchDTO
import com.btlcnpm.androidapp.data.model.Review
import com.btlcnpm.androidapp.data.remote.BetaCinemaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

// Repository để xử lý các tác vụ liên quan đến Phim, Tìm kiếm và Đánh giá
class MovieRepository(private val apiService: BetaCinemaApi) {

    // Lấy tất cả phim
    suspend fun getAllMovies(): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllMovies()
                if (response.isSuccessful && response.body() != null) {
                    Log.d("MovieRepository", "Fetched ${response.body()?.size ?: 0} movies.")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Failed to get movies"
                    Log.e("MovieRepository", "Get all movies failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("MovieRepository", "Network error getting all movies: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("MovieRepository", "Unexpected error getting all movies: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Lấy phim theo ID
    suspend fun getMovieById(movieId: String): Result<Movie> {
        return withContext(Dispatchers.IO) {
            try {
                // Gọi API getMovieById từ BetaCinemaApi
                val response = apiService.getMovieById(movieId)
                if (response.isSuccessful && response.body() != null) {
                    Log.d("MovieRepo", "Fetched movie with ID: $movieId")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Movie not found or error"
                    Log.e("MovieRepo", "Get movie by ID failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("MovieRepo", "Network error getting movie by ID: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("MovieRepo", "Unexpected error getting movie by ID: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Tìm kiếm phim
    suspend fun searchMovies(query: String): Result<List<MovieSearchDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                // Gọi API searchMovies từ BetaCinemaApi
                val response = apiService.searchMovies(query)
                if (response.isSuccessful && response.body() != null) {
                    Log.d("MovieRepo", "Search found ${response.body()?.size ?: 0} movies for query: $query")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Search failed"
                    Log.e("MovieRepo", "Search movies failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("MovieRepo", "Network error searching movies: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("MovieRepo", "Unexpected error searching movies: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // --- CÁC HÀM MỚI CHO REVIEW ---

    // Lấy đánh giá theo Movie ID
    suspend fun getReviewsByMovieId(movieId: String): Result<List<Review>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getReviewsByMovieId(movieId) // Gọi API mới
                if (response.isSuccessful && response.body() != null) {
                    Log.d("MovieRepo", "Fetched ${response.body()?.size ?: 0} reviews for movie: $movieId")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Failed to get reviews"
                    Log.e("MovieRepo", "Get reviews failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("MovieRepo", "Network error getting reviews: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("MovieRepo", "Unexpected error getting reviews: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Tạo đánh giá mới
    suspend fun createReview(request: CreateReviewRequest): Result<Review> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createReview(request) // Gọi API mới
                if (response.isSuccessful && response.body() != null) {
                    Log.d("MovieRepo", "Review created successfully: ${response.body()?.reviewId}")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Failed to create review"
                    Log.e("MovieRepo", "Create review failed: ${response.code()} - $errorBody")
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Log.e("MovieRepo", "Network error creating review: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("MovieRepo", "Unexpected error creating review: ${e.message}")
                Result.failure(e)
            }
        }
    }
}

