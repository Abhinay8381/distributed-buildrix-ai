package com.abhinay.buildrix.account_service.service.impl;

import com.abhinay.buildrix.account_service.dto.billing.subscription.CheckoutRequest;
import com.abhinay.buildrix.account_service.dto.billing.subscription.CheckoutResponse;
import com.abhinay.buildrix.account_service.dto.billing.subscription.PortalResponse;
import com.abhinay.buildrix.account_service.entity.Plan;
import com.abhinay.buildrix.account_service.entity.Subscription;
import com.abhinay.buildrix.account_service.entity.User;
import com.abhinay.buildrix.account_service.mapper.SubscriptionMapper;
import com.abhinay.buildrix.account_service.repository.PlanRepository;
import com.abhinay.buildrix.account_service.repository.SubscriptionRepository;
import com.abhinay.buildrix.account_service.repository.UserRepository;
import com.abhinay.buildrix.account_service.service.PaymentProcessor;
import com.abhinay.buildrix.account_service.service.SubscriptionService;
import com.abhinay.buildrix.common_lib.dto.SubscriptionResponse;
import com.abhinay.buildrix.common_lib.enums.SubscriptionStatus;
import com.abhinay.buildrix.common_lib.exceptions.BadRequestException;
import com.abhinay.buildrix.common_lib.exceptions.ResourceNotFoundException;
import com.abhinay.buildrix.common_lib.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserRepository userRepository;
    private final PaymentProcessor paymentProcessor;
    private final AuthUtil authUtil;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    //private final ProjectMemberRepository projectMemberRepository;

    private static final int FREE_TIER_PROJECT_LIMIT = 100;

    @Override
    public PortalResponse openCustomerPortal() {
        User user = getUserById(authUtil.getCurrentUserId());
        String stripeCustomerId = user.getStripeCustomerId();

        if(stripeCustomerId == null || stripeCustomerId.isBlank()) {
            throw new BadRequestException("Stripe customer Id not found for user with id: " + user.getId());
        }
        String customerPortalUrl = paymentProcessor.openCustomerPortal(stripeCustomerId);
        return new PortalResponse(customerPortalUrl);
    }

    @Override
    public CheckoutResponse createCheckout(CheckoutRequest request) {
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.planId().toString()));

        UUID userId = authUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        String checkOutUrl = paymentProcessor.checkout(plan, user);
        return new CheckoutResponse(checkOutUrl);
    }

    @Override
    public SubscriptionResponse getUserSubscription() {
        UUID userId = authUtil.getCurrentUserId();
        Subscription subscription = subscriptionRepository.findByUserIdAndStatusIn(userId, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE,
                        SubscriptionStatus.TRAILING))
                .orElse(new Subscription());
        return subscriptionMapper.toResponse(subscription);
    }

    @Transactional
    @Override
    public void activateSubscription(UUID userId, UUID planId, String gatewaySubscriptionId, String customerId) {
        if(subscriptionRepository.existsByGatewaySubscriptionId(gatewaySubscriptionId))
            return;
        User user = getUserById(userId);
        Plan plan = getPlanById(planId);
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .gatewaySubscriptionId(gatewaySubscriptionId)
                .status(SubscriptionStatus.INCOMPLETE)
                .build();
        subscriptionRepository.save(subscription);
    }

    @Transactional
    @Override
    public void updateSubscription(String gatewaySubId,
                                   SubscriptionStatus status,
                                   Instant periodStart,
                                   Instant periodEnd,
                                   Boolean cancelAtPeriodEnd, UUID planId) {

        boolean hasSubUpdated = false;
        Subscription subscription = getSubscription(gatewaySubId);
        if(status != null && !status.equals(subscription.getStatus())){
            subscription.setStatus(status);
            hasSubUpdated = true;
        }

        if(periodEnd != null && !periodEnd.equals(subscription.getCurrentPeriodEnd())){
            subscription.setCurrentPeriodEnd(periodEnd);
            hasSubUpdated = true;
        }

        if(periodStart != null && !periodStart.equals(subscription.getCurrentPeriodStart())){
            subscription.setCurrentPeriodStart(periodStart);
            hasSubUpdated = true;
        }
        if(cancelAtPeriodEnd != null && !cancelAtPeriodEnd.equals(subscription.getCancelAtPeriodEnd())){
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            hasSubUpdated = true;
        }
        if(planId != null && !planId.equals(subscription.getPlan().getId())) {
            subscription.setPlan(getPlanById(planId));
            hasSubUpdated = true;
        }

        if(hasSubUpdated){
            log.info("Subscription with gateway id: {} updated", gatewaySubId);
            subscriptionRepository.save(subscription);
        }
    }

    @Override
    public void cancelSubscription(String id) {
        Subscription subscription = getSubscription(id);
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscriptionRepository.save(subscription);
    }

    @Transactional
    @Override
    public void renewSubscription(String gatewaySubId, Instant periodStart, Instant periodEnd,
                                  String customerEmail, String customerId) {
        Subscription subscription = subscriptionRepository.findByGatewaySubscriptionId(gatewaySubId)
                .orElseGet(() -> createSubscriptionFromStripe(gatewaySubId, customerEmail, customerId)); // <--- Fallback if invoice.paid arrives first

        if(subscription.getStatus() == SubscriptionStatus.INCOMPLETE ||
                subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }
        Instant currentPeriodStart = periodStart == null ? subscription.getCurrentPeriodEnd() : periodStart;
        subscription.setCurrentPeriodStart(currentPeriodStart);
        subscription.setCurrentPeriodEnd(periodEnd);
        subscriptionRepository.save(subscription);
    }


    @Transactional
    @Override
    public void markSubscriptionDue(String subId) {
        Subscription subscription = getSubscription(subId);

        if(subscription.getStatus() == SubscriptionStatus.PAST_DUE){
            log.debug("Subscription with gateway id: {} is already marked as PAST_DUE", subId);
            return;
        }

        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);
    }


    private User getUserById(UUID userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    private Plan getPlanById(UUID planId){
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId.toString()));
    }

    private Subscription getSubscription(String gatewaySubId) {
        return subscriptionRepository.findByGatewaySubscriptionId(gatewaySubId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", gatewaySubId));
    }


    private Subscription createSubscriptionFromStripe(String gatewaySubId,
                                                      String stripePriceId,
                                                      String customerEmail) {
        User user = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", customerEmail));
        Plan plan = planRepository.findByStripePriceId(stripePriceId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", stripePriceId));
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .gatewaySubscriptionId(gatewaySubId)
                .status(SubscriptionStatus.INCOMPLETE)
                .build();
        subscriptionRepository.save(subscription);
        return subscription;
    }

}
