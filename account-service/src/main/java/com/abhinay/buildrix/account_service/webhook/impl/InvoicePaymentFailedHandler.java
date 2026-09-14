package com.abhinay.buildrix.account_service.webhook.impl;

import com.abhinay.buildrix.account_service.webhook.StripeEventHandler;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.abhinay.buildrix.account_service.service.SubscriptionService;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoicePaymentFailedHandler implements StripeEventHandler {

    private final SubscriptionService subscriptionService;

    @Override
    public String getEventType() {
        return "invoice.payment_failed";
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

        subscriptionService.markSubscriptionDue(subId);
    }

    private String extractSubscriptionId(Invoice invoice) {
        var parent = invoice.getParent();
        if(parent == null) return null;

        var subDetails = parent.getSubscriptionDetails();
        if(subDetails == null) return null;

        return subDetails.getSubscription();
    }
}
