package io.mosip.certify.soapclientdataprovider.integration.service;

import io.mosip.certify.gen.SRELServiceService;
import io.mosip.certify.gen.SimpleConsultaDniService;
import jakarta.xml.ws.BindingProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.util.Map;

@Configuration
public class SoapConfig {

    @Bean
    public SRELServiceService srelServiceService() throws MalformedURLException {
        SimpleConsultaDniService serviceImpl = new SimpleConsultaDniService();
        SRELServiceService service = serviceImpl.getSRELServiceServicePort();

        // If you need to set custom properties like timeout, SSL, etc.
        BindingProvider bindingProvider = (BindingProvider) service;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();

        // Set timeouts if needed
        requestContext.put("com.sun.xml.ws.connect.timeout", 30000);
        requestContext.put("com.sun.xml.ws.request.timeout", 30000);

        return service;
    }
}