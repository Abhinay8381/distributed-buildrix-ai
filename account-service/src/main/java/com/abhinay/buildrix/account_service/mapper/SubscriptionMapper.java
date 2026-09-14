package com.abhinay.buildrix.account_service.mapper;

import com.abhinay.buildrix.account_service.entity.Subscription;
import com.abhinay.buildrix.common_lib.dto.SubscriptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionMapper {

    SubscriptionResponse toResponse(Subscription subscription);
}
