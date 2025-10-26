package com.btlcnpm.androidapp.ui.common // Thay package của bạn

import androidx.compose.foundation.clickable // Cho phép nhấn vào item
import androidx.compose.foundation.layout.* // Các layout cơ bản (Column, padding,...)
import androidx.compose.material3.Card       // Tạo hiệu ứng thẻ nổi
import androidx.compose.material3.MaterialTheme // Lấy style chữ
import androidx.compose.material3.Text         // Hiển thị chữ
import androidx.compose.runtime.Composable      // Annotation bắt buộc cho Composable
import androidx.compose.ui.Modifier         // Để tùy chỉnh Composable (kích thước, padding,...)
import androidx.compose.ui.layout.ContentScale // Cách hiển thị ảnh (crop, fit,...)
import androidx.compose.ui.platform.LocalContext // Lấy context hiện tại (cần cho Coil)
import androidx.compose.ui.text.style.TextOverflow // Hiển thị '...' nếu text quá dài
import androidx.compose.ui.unit.dp              // Đơn vị kích thước (density-independent pixel)
import coil.compose.AsyncImage                // Composable để tải ảnh từ URL
import coil.request.ImageRequest              // Để cấu hình việc tải ảnh (placeholder, error)
import com.btlcnpm.androidapp.data.MovieDTO // Import DTO

// Annotation @Composable đánh dấu đây là một hàm vẽ giao diện
@Composable
fun MoviePosterItem(
    movie: MovieDTO,                // Dữ liệu phim cần hiển thị
    onClick: (String) -> Unit,      // Hàm sẽ được gọi khi item được nhấn, nhận vào movieId
    modifier: Modifier = Modifier   // Modifier để tùy chỉnh từ bên ngoài (nếu cần)
) {
    // Card tạo hiệu ứng nổi và bo góc
    Card(
        modifier = modifier
            .padding(4.dp) // Khoảng cách 4dp xung quanh Card
            .fillMaxWidth() // Chiếm hết chiều ngang của cột trong Grid
            .clickable { onClick(movie.movieId) } // Đăng ký sự kiện nhấn
    ) {
        // Column xếp các thành phần con theo chiều dọc
        Column {
            // AsyncImage dùng thư viện Coil để tải ảnh
            AsyncImage(
                // Tạo ImageRequest để cấu hình Coil
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl) // URL lấy từ DTO
                    .crossfade(true) // Hiệu ứng mờ dần khi ảnh hiện ra
                    // .placeholder(R.drawable.placeholder_image) // Ảnh hiển thị khi chờ tải (tùy chọn)
                    // .error(R.drawable.error_image) // Ảnh hiển thị khi tải lỗi (tùy chọn)
                    .build(),
                contentDescription = movie.title, // Mô tả ảnh (quan trọng cho người khiếm thị)
                modifier = Modifier
                    .fillMaxWidth() // Chiếm hết chiều ngang của Column
                    .aspectRatio(2 / 3f), // Giữ tỷ lệ khung hình 2:3 (poster)
                contentScale = ContentScale.Crop // Cắt ảnh để lấp đầy khung, giữ tỷ lệ
            )
            // Text hiển thị tên phim
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleSmall, // Style chữ nhỏ, phù hợp với Grid item
                maxLines = 2, // Giới hạn 2 dòng
                overflow = TextOverflow.Ellipsis, // Hiển thị dấu "..." nếu text dài hơn 2 dòng
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp) // Padding quanh text
            )
        }
    }
}