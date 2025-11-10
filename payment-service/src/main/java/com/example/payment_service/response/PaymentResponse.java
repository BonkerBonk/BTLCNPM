package com.example.payment_service.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PaymentResponse implements Serializable {
    private String status;
    private String message;
    private String URL;

}
