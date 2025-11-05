package com.btlcnpm.CheckinService.service;

import com.btlcnpm.CheckinService.model.Ticket;
import com.btlcnpm.CheckinService.repository.CheckinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private CheckinRepository checkinRepository;

    @Override
    public Ticket processCheckin(String qrCodeData) throws Exception {

        // TÌM VÉ
        Ticket ticket = checkinRepository.getTicketById(qrCodeData);

        if (ticket == null) {

            throw new Exception("Vé không hợp lệ.");
        }

        // KIỂM TRA STATUS
        if ("USED".equals(ticket.getStatus())) {
            // Lỗi theo đề cương: "Vé đã được sử dụng"
            throw new Exception("Vé đã được sử dụng.");
        }

        if ("EXPIRED".equals(ticket.getStatus())) {
            throw new Exception("Vé đã hết hạn.");
        }

        if (!"VALID".equals(ticket.getStatus())) {
            // Trạng thái lạ
            throw new Exception("Vé không hợp lệ (trạng thái không rõ).");
        }

        // CẬP NHẬT
        // Đây là trường hợp thành công, vé "VALID"
        checkinRepository.updateTicketStatus(qrCodeData, "USED");

        // Cập nhật đối tượng hiện tại để trả về
        ticket.setStatus("USED");
        return ticket;
    }
}