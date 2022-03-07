package com.ksb.spring.vol2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

public class ServiceRequest {
    String customerNo; //폼에서 입력받은 코객번호를 저장할 프로퍼티
    String ProductNo;
    String description;
    @Autowired EmailService emailService;

    public void notifyServiceRequestRegistration(){
        if(this.customer.serviceNotificationMethod == NotificationMethd.EMAIL){
            this.emailService.sendEmail(customer.getEmail(), "A/S 접수 완료");
        }
    }

    public String getCustomerNo() {
        return customerNo;
    }

    public void setCustomerNo(String customerNo) {
        this.customerNo = customerNo;
    }

    public String getProductNo() {
        return ProductNo;
    }

    public void setProductNo(String productNo) {
        ProductNo = productNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }}
