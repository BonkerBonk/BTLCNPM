package com.btlcnpm.CheckinService.service;

import com.btlcnpm.CheckinService.model.Ticket;

public interface CheckinService {
    Ticket processCheckin(String qrCodeData) throws Exception;
}