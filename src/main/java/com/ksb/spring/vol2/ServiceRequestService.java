package com.ksb.spring.vol2;

public class ServiceRequestService {
    public void addNewService(ServiceRequest serviceRequest){
        this.serviceRequestDao.add(serviceRequest);
        serviceRequest.notifyServiceRequestRegistration();
    }
}
