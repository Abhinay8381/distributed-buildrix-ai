package com.abhinay.buildrix.account_service.webhook;

import com.stripe.model.StripeObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StripeEventRouter {

    private final Map<String, StripeEventHandler> eventHandlers;

    public StripeEventRouter(List<StripeEventHandler> handlerList) {
        this.eventHandlers = handlerList.stream()
                .collect(Collectors.toMap(StripeEventHandler::getEventType, h -> h));
    }

    public void routeEvent(String type, StripeObject stripeObject, Map<String, String> metaData){
        StripeEventHandler handler = eventHandlers.get(type);
        if(handler == null){
            log.warn("Unhandled event type: {},  ignoring.....", type);
        }else{
            handler.processEvent(stripeObject, metaData);
        }
    }
}
