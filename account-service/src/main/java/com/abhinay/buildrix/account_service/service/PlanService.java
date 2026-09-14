package com.abhinay.buildrix.account_service.service;



import com.abhinay.buildrix.common_lib.dto.PlanResponse;

import java.util.List;

public interface PlanService {
    List<PlanResponse> getAllActivePlans();
}
