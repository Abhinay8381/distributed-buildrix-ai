package com.abhinay.buildrix.account_service.dto.billing.subscription;

import java.util.UUID;

public record CheckoutRequest(
        UUID planId
) {
}
