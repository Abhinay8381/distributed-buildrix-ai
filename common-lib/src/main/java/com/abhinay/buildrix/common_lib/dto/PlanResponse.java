package com.abhinay.buildrix.common_lib.dto;

import java.util.UUID;

public record PlanResponse(
         UUID id,
         String name,
         Integer maxProjects,
         Integer maxTokensPerDay,
         Integer maxPreviews,
         Boolean unlimitedAi,
         String features,
         String price
) {
}
