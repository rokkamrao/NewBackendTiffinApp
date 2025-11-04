package com.tiffin.api.notification.dto;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class NotificationData {
    private String dataOrderId;
    private String dataPaymentId;
    private String dataSubscriptionId;
    private String dataStatus;
    private String dataAdditionalInfo;
}
