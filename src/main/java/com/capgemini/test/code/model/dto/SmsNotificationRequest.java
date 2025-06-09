package com.capgemini.test.code.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsNotificationRequest {
    private String phone;
    private String message;
}
