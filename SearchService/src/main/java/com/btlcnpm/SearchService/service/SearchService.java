package com.btlcnpm.SearchService.service;

import com.btlcnpm.SearchService.dto.MovieSearchDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class SearchService {

    private final Firestore db;

    // Tiêm (Inject) đối tượng Firestore đã được tạo ở FirebaseConfig
    public SearchService(Firestore firestore) {
        this.db = firestore;
    }

    /**
     * Tìm kiếm phim theo tên (Tìm kiếm dạng prefix "bắt đầu bằng...")
     * @param query Tên phim cần tìm
     * @return Danh sách phim khớp
     */
    public List<MovieSearchDTO> searchMovies(String query) throws ExecutionException, InterruptedException {
        List<MovieSearchDTO> movies = new ArrayList<>();

        // (Lưu ý: Firestore không hỗ trợ tìm "contains" (chứa)
        // Chúng ta đang dùng cách tìm "starts-with" (bắt đầu bằng)
        // \uf8ff là một ký tự Unicode rất cao, giúp tạo ra một dải tìm kiếm
        ApiFuture<QuerySnapshot> future = db.collection("movies")
                .orderBy("title") // Phải sắp xếp theo trường 'title'
                .startAt(query)   // Bắt đầu từ 'query'
                .endAt(query + "\uf8ff") // Kết thúc ở 'query' + ký tự đặc biệt
                .limit(20) // Giới hạn 20 kết quả
                .get();

        // Lấy kết quả
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        // Chuyển đổi kết quả từ Firestore sang DTO
        for (QueryDocumentSnapshot document : documents) {
            movies.add(document.toObject(MovieSearchDTO.class));
        }

        return movies;
    }
}