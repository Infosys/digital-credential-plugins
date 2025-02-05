package io.mosip.certify.peruiddataprovider.integration.service;

import io.mosip.certify.peruiddataprovider.integration.dto.request.ConsultaArg;
import io.mosip.certify.peruiddataprovider.integration.dto.response.ResponseEnvelope;
import io.mosip.certify.peruiddataprovider.integration.dto.response.ResponseReturn;
import io.mosip.certify.util.SoapUtil;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringReader;

@Service
@Slf4j
public class ConsultaDniService {

    public ResponseReturn getConsultarResponse(ConsultaArg consultaArg, String endpointUri) throws Exception {
        try {
            // SOAP Request Body
            String soapRequest = SoapUtil.buildSoapRequest(consultaArg);
            // Send SOAP request and get response
            String soapResponse = SoapUtil.sendSOAPRequest(endpointUri, soapRequest);

            try {
                ResponseEnvelope responseEnvelope = SoapUtil.getResponseEnvelope(soapResponse);

                if(responseEnvelope != null && responseEnvelope.getResponseBody() != null && responseEnvelope.getResponseBody().getConsultarResponse() != null) {
                    return responseEnvelope.getResponseBody().getConsultarResponse().getResponseReturn();
                }

            } catch (Exception e) {
                log.info("Error during deserialization", e);
                throw new Exception("ERROR_DURING_DESERIALIZATION");
            }
        } catch (Exception e) {
            log.info("Failed to get response from the resource", e);
        }

        throw new Exception("FAILED_TO_GET_RESPONSE");
    }
}