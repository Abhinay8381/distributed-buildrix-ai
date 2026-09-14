package com.abhinay.buildrix.workspace_service.client;

import com.abhinay.buildrix.common_lib.dto.SubscriptionResponse;
import com.abhinay.buildrix.common_lib.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@FeignClient(name = "account-service", path = "/account/internal/user")
public interface AccountServiceClient {

    @GetMapping("/find-by-email")
    UserDto findUserByEmail(@RequestParam("email") String email);

    @GetMapping("/get-user-subscription")
    SubscriptionResponse getUserSubscription();

    @GetMapping("/find-by-id/{id}")
    UserDto findUserById(@PathVariable UUID id);

    @PostMapping("/find-by-ids")
    List<UserDto> findUserByIds(@RequestBody Set<UUID> ids);
}
