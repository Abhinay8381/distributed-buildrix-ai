package com.abhinay.buildrix.account_service.controller;

import com.abhinay.buildrix.account_service.api.UserLookupService;
import com.abhinay.buildrix.account_service.service.SubscriptionService;
import com.abhinay.buildrix.common_lib.dto.SubscriptionResponse;
import com.abhinay.buildrix.common_lib.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/user")
public class InternalUserController {

    private final UserLookupService userLookupService;
    private final SubscriptionService subscriptionService;

    @GetMapping("/find-by-email")
    UserDto findUserByEmail(@RequestParam("email") String email){
        return userLookupService.findByEmail(email);
    }

    @GetMapping("/find-by-id/{id}")
    UserDto findUserById(@PathVariable UUID id){
        return userLookupService.findById(id);
    }

    @PostMapping("/find-by-ids")
    List<UserDto> findUserByIds(@RequestBody Set<UUID> ids){
        return userLookupService.findByIds(ids);
    }

    @GetMapping("/get-user-subscription")
    SubscriptionResponse subscriptionResponse(){
        return subscriptionService.getUserSubscription();
    }
}
