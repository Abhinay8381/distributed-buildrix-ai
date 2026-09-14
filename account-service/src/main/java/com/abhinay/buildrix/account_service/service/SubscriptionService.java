package com.abhinay.buildrix.account_service.service;

import com.abhinay.buildrix.account_service.dto.billing.subscription.CheckoutRequest;
import com.abhinay.buildrix.account_service.dto.billing.subscription.CheckoutResponse;
import com.abhinay.buildrix.account_service.dto.billing.subscription.PortalResponse;
import com.abhinay.buildrix.common_lib.dto.SubscriptionResponse;
import com.abhinay.buildrix.common_lib.enums.SubscriptionStatus;

import java.time.Instant;
import java.util.UUID;

public interface SubscriptionService {
    PortalResponse openCustomerPortal();

    CheckoutResponse createCheckout(CheckoutRequest request);

    SubscriptionResponse getUserSubscription();


    void activateSubscription(UUID userId, UUID planId, String subscriptionId, String customerId);

    void updateSubscription(String id, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, UUID planId);

    void cancelSubscription(String id);

    void renewSubscription(String subId, Instant periodStart, Instant periodEnd, String customerEmail, String stripePriceId);

    void markSubscriptionDue(String subId);

    //boolean canCreateProject();
}
