package com.abhinay.buildrix.account_service.service;


import com.abhinay.buildrix.account_service.entity.Plan;
import com.abhinay.buildrix.account_service.entity.User;

public interface PaymentProcessor {

     String  openCustomerPortal(String stripeCustomerId);

     String checkout(Plan plan, User user);

}
