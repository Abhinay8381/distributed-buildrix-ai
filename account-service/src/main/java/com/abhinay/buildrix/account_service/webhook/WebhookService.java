package com.abhinay.buildrix.account_service.webhook;

public interface WebhookService {

    void processWebhook(String payload, String sigHeader);
}
