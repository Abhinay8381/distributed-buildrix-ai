package com.abhinay.buildrix.account_service.service.impl;

import com.abhinay.buildrix.account_service.entity.Plan;
import com.abhinay.buildrix.account_service.entity.User;
import com.abhinay.buildrix.account_service.service.PaymentProcessor;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProcessor implements PaymentProcessor {


    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public String openCustomerPortal(String stripeCustomerId) {
        try {
            com.stripe.model.billingportal.Session session = com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(stripeCustomerId)
                            .setReturnUrl(frontendUrl)
                            .build()
            );
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String checkout(Plan plan, User user) {

        var sessionCreateParams = SessionCreateParams.builder()
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(plan.getStripePriceId())
                        .setQuantity(1L)
                        .build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(new SessionCreateParams.SubscriptionData.Builder()
                        .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                .build())
                        .build())
                .setSuccessUrl(frontendUrl + "/payment/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/payment/cancel.html")
                .putMetadata("user_id", user.getId().toString())
                .putMetadata("plan_id", plan.getId().toString());

        try{
            if(user.getStripeCustomerId() != null && !user.getStripeCustomerId().isBlank()){
                sessionCreateParams.setCustomer(user.getStripeCustomerId());
            }else
                sessionCreateParams.setCustomerEmail(user.getEmail());
            Session session = Session.create(sessionCreateParams.build());
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }



}
