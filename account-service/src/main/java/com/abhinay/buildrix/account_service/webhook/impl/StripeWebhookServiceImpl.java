package com.abhinay.buildrix.account_service.webhook.impl;

import com.abhinay.buildrix.account_service.webhook.StripeEventRouter;
import com.abhinay.buildrix.account_service.webhook.WebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookServiceImpl implements WebhookService {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final StripeEventRouter stripeEventRouter;

    @Override
    public void processWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader,webhookSecret);
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = null;
            if(deserializer.getObject().isPresent()){
                stripeObject = deserializer.getObject().get();
            }
            else {
                //Fallback: for old SDK
                try {
                    stripeObject = deserializer.deserializeUnsafe();
                    if(stripeObject == null){
                        log.warn("Failed to deserialize webhook object for event: {}", event.getType());
                    }
                } catch (Exception e) {
                    log.error("Unsafe deserializing failed for webhook event: {}, {}", event.getType(), e.getMessage());
                    throw  new RuntimeException("Deserialisation failed");
                }
            }

            Map<String, String> metaData = new HashMap<>();
            if(stripeObject instanceof Session session)
                metaData = session.getMetadata();

           handleWebhookEvent(event.getType(), stripeObject, metaData);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metaData) {
        stripeEventRouter.routeEvent(type, stripeObject, metaData);
    }
    
}
