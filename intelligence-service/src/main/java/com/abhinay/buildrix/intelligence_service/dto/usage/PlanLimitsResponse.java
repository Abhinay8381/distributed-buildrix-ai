package com.abhinay.buildrix.intelligence_service.dto.usage;

public record PlanLimitsResponse (
        String planName,
        Integer maxTokensPerDay,
        Integer maxProjects,
        Boolean unlimitedAi
){
}
