package com.abhinay.buildrix.account_service.webhook.impl;

import com.abhinay.buildrix.account_service.webhook.StripeEventHandler;
import com.abhinay.buildrix.account_service.service.SubscriptionService;

import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerSubscriptionDeletedHandler implements StripeEventHandler {

    private final SubscriptionService subscriptionService;

    @Override
    public String getEventType() {
        return "customer.subscription.deleted";
    }

    @Override
    public void processEvent(StripeObject stripeObject, Map<String, String> metaData) {
        Subscription subscription = (Subscription) stripeObject;

        if(subscription == null){
            log.error("Stripe subscription is null for event: customer.subscription.deleted");
            return;
        }

        subscriptionService.cancelSubscription(subscription.getId());
    }


}
