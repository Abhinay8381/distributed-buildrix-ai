package com.abhinay.buildrix.account_service.webhook.impl;

import com.abhinay.buildrix.account_service.entity.User;
import com.abhinay.buildrix.account_service.repository.UserRepository;

import com.abhinay.buildrix.account_service.service.SubscriptionService;
import com.abhinay.buildrix.account_service.webhook.StripeEventHandler;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckoutSessionCompletedHandler implements StripeEventHandler {

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Override
    public String getEventType() {
        return "checkout.session.completed";
    }

    @Transactional
    @Override
    public void processEvent(StripeObject stripeObject, Map<String, String> metaData) {
        Session session = (Session) stripeObject;

        if(session == null){
            log.error("Stripe session is null for event: checkout.session.completed");
            return;
        }

        UUID userId = UUID.fromString(metaData.get("user_id"));
        UUID planId = UUID.fromString(metaData.get("plan_id"));

        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        if(user.getStripeSubscriptionId() == null){
            user.setStripeSubscriptionId(subscriptionId);
            user.setStripeCustomerId(customerId);
            userRepository.save(user);
        }
        subscriptionService.activateSubscription(userId, planId, subscriptionId, customerId);
    }
}
