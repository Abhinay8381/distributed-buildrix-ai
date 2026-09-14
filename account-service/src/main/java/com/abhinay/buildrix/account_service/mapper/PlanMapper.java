package com.abhinay.buildrix.account_service.mapper;
import com.abhinay.buildrix.account_service.entity.Plan;
import com.abhinay.buildrix.common_lib.dto.PlanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlanMapper {

    PlanResponse toResponse(Plan plan);
}
