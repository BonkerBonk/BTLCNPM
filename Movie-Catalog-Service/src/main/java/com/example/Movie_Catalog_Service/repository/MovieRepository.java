package com.example.Movie_Catalog_Service.repository;



import com.example.Movie_Catalog_Service.model.Movie;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class MovieRepository {

    private static final String COLLECTION_NAME = "movies";

    public Movie save(Movie movie) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference collection = db.collection(COLLECTION_NAME);

        int index = collection.get().get().size() + 1;
        String newId = "MOV" + index;
        movie.setMovieId(newId);

        collection.document(newId).set(movie);
        return movie;
    }

    public List<Movie> getAll() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> docs = query.get().getDocuments();
        List<Movie> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            result.add(doc.toObject(Movie.class));
        }
        return result;
    }

    public Movie getById(String id) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(id).get().get();
        return snapshot.exists() ? snapshot.toObject(Movie.class) : null;
    }

    public void deleteById(String id) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(id).delete();
    }
}