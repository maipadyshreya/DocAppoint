
package com.example.docAppointment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReq{

    private String notificationId;
    private String recipient;
    private String type;
    private String message;
}
