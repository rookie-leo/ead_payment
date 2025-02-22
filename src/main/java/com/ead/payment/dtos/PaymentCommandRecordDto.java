package com.ead.payment.dtos;

import java.util.UUID;

public record PaymentCommandRecordDto(
        UUID userId,
        UUID paymentId,
        UUID cardId
) {
}
