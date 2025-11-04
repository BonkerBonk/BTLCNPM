package com.btlcnpm.TicketService.repository;
// Thêm các import này
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import com.btlcnpm.TicketService.model.Ticket;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TicketRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "tickets";

    private CollectionReference getTicketCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    public String save(Ticket ticket) throws Exception {
        String ticketId = getTicketCollection().document().getId();
        ticket.setTicketId(ticketId);
        ticket.setQrCodeData(ticketId); // Dùng chính ID làm dữ liệu QR

        ApiFuture<WriteResult> future = getTicketCollection().document(ticketId).set(ticket);
        future.get();
        return ticketId;
    }


    public List<Ticket> getTicketsByUserId(String userId) throws Exception {
        CollectionReference ticketsCollection = getTicketCollection();

        // 1. Tạo câu truy vấn: WHERE userId == [userId]
        Query query = ticketsCollection.whereEqualTo("userId", userId);

        // 2. Thực thi truy vấn
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        // 3. Chuyển kết quả thành danh sách (List)
        List<Ticket> tickets = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            tickets.add(document.toObject(Ticket.class));
        }

        return tickets;
    }
    // TODO: Thêm hàm getTicketsByUserId(String userId)
}