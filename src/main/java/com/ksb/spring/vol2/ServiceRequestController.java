package com.ksb.spring.vol2;

import com.ksb.spring.vol1.jaxb.ObjectFactory;

import javax.annotation.Resource;
import java.security.Provider;

public class ServiceRequestController {
    @Inject
    Provider<ServiceRequest> serviceRequestProvider;

    public void serviceRequestFormSummit(HttpServletRequest request){
        ServiceRequest serviceRequest = this.serviceRequestProvider.get();
        serviceRequest.setCustomerNo(request.getParameter("custno"));
        ...
    }
}
