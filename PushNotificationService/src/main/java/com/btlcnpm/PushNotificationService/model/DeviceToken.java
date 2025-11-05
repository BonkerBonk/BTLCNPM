package com.btlcnpm.PushNotificationService.model;

import lombok.Data;
import java.util.List;

@Data
public class DeviceToken {


    private String userId;

    private List<String> tokens;
}
    
