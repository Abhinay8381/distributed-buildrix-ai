package com.abhinay.buildrix.account_service.service.impl;

import com.abhinay.buildrix.account_service.service.PlanService;
import com.abhinay.buildrix.common_lib.dto.PlanResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {

    @Override
    public List<PlanResponse> getAllActivePlans() {
        return List.of();
    }
}
