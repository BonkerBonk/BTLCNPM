package com.btlcnpm.CheckinService.repository;

import com.btlcnpm.CheckinService.model.Ticket; // Model vừa copy
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CheckinRepository {

    @Autowired
    private Firestore firestore;

    private static final String TICKET_COLLECTION = "tickets";


    public Ticket getTicketById(String ticketId) throws Exception {

        DocumentReference docRef = firestore.collection(TICKET_COLLECTION).document(ticketId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(Ticket.class);
        } else {
            return null; // Không tìm thấy vé
        }
    }


     //Cập nhật trạng thái của một ticket

    public void updateTicketStatus(String ticketId, String newStatus) throws Exception {
        DocumentReference docRef = firestore.collection(TICKET_COLLECTION).document(ticketId);

        // Chỉ cập nhật trường "status"
        ApiFuture<WriteResult> future = docRef.update("status", newStatus);

        future.get(); // Chờ cập nhật xong
    }
}