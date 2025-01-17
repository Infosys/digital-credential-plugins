package io.mosip.certify.soapdataprovider.integration.service;//package io.mosip.certify.soapdataprovider.integration.service;
//
//import io.mosip.certify.wsdl.GetCountryRequest;
//import io.mosip.certify.wsdl.GetCountryResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
//import org.springframework.ws.soap.client.core.SoapActionCallback;
//
//
//public class CountryClient extends WebServiceGatewaySupport {
//
//    private static final Logger log = LoggerFactory.getLogger(CountryClient.class);
//
//    public GetCountryResponse getCountry(String country) {
//
//        GetCountryRequest request = new GetCountryRequest();
//        request.setName(country);
//
//        log.info("Requesting location for " + country);
//
//        GetCountryResponse response = (GetCountryResponse) getWebServiceTemplate()
//                .marshalSendAndReceive("http://localhost:8080/ws/countries", request,
//                        new SoapActionCallback(
//                                "http://spring.io/guides/gs-producing-web-service/GetCountryRequest"));
//
//        return response;
//    }
//
//}




import io.mosip.certify.wsdl.ListOfCountryNamesByName;
import io.mosip.certify.wsdl.ListOfCountryNamesByNameResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

@Service
public class CountryClient {

    @Autowired
    private WebServiceTemplate webServiceTemplate;

    public ListOfCountryNamesByNameResponse getListOfCountriesByName() {
        ListOfCountryNamesByName request = new ListOfCountryNamesByName();
        ListOfCountryNamesByNameResponse response = (ListOfCountryNamesByNameResponse) webServiceTemplate.marshalSendAndReceive(request);

        return response;
    }

}
