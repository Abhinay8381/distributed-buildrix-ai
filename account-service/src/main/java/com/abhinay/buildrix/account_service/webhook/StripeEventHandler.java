package com.abhinay.buildrix.account_service.webhook;

import com.stripe.model.StripeObject;

import java.util.Map;

public interface StripeEventHandler {

    String getEventType();

    void processEvent(StripeObject stripeObject, Map<String, String> metaData);
}
