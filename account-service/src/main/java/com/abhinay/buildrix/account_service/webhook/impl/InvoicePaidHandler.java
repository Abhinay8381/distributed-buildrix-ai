package com.abhinay.buildrix.account_service.webhook.impl;

import com.abhinay.buildrix.account_service.service.SubscriptionService;
import com.abhinay.buildrix.account_service.webhook.StripeEventHandler;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j

public class InvoicePaidHandler implements StripeEventHandler {

    private final SubscriptionService subscriptionService;

    @Override
    public String getEventType() {
        return "invoice.paid";
    }

    @Override
    public void processEvent(StripeObject stripeObject, Map<String, String> metaData) {
        Invoice invoice = (Invoice) stripeObject;

        if(invoice == null){
            log.warn("Stripe invoice is null for event: invoice.paid");
            return;
        }
        String subId = extractSubscriptionId(invoice);
        if(subId == null) return;

        try {
            Subscription subscription = Subscription.retrieve(subId);
            SubscriptionItem item = subscription.getItems().getData().getFirst();

            Instant periodStart = toInstant(item.getCurrentPeriodStart());
            Instant periodEnd = toInstant(item.getCurrentPeriodEnd());


            String customerEmail = Customer.retrieve(subscription.getCustomer()).getEmail();
            String stripePriceId = subscription.getItems().getData().getFirst().getPrice().getId();

            subscriptionService.renewSubscription(
                    subId,
                    periodStart,
                    periodEnd,
                    stripePriceId,
                    customerEmail
            );
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractSubscriptionId(Invoice invoice) {
        var parent = invoice.getParent();
        if(parent == null) return null;

        var subDetails = parent.getSubscriptionDetails();
        if(subDetails == null) return null;

        return subDetails.getSubscription();
    }

    private Instant toInstant(Long epoch) {
        return epoch == null ? null: Instant.ofEpochSecond(epoch);
    }
}
