package com.btlcnpm.PaymentService.service;

import com.btlcnpm.PaymentService.dto.CheckoutRequest;

public interface PaymentService {
    String processPayment(CheckoutRequest request) throws Exception;
}