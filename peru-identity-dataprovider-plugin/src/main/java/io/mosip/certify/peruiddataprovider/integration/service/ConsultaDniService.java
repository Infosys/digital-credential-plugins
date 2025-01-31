package io.mosip.certify.peruiddataprovider.integration.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.mosip.certify.peruiddataprovider.integration.dto.request.ConsultaArg;
import io.mosip.certify.peruiddataprovider.integration.dto.response.ResponseEnvelope;
import io.mosip.certify.peruiddataprovider.integration.dto.response.ResponseReturn;
import io.mosip.certify.util.SoapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
                XmlMapper xmlMapper = new XmlMapper();
                ResponseEnvelope responseEnvelope = xmlMapper.readValue(soapResponse, ResponseEnvelope.class);

                if(responseEnvelope != null && responseEnvelope.getBody() != null && responseEnvelope.getBody().getConsultarResponse() != null) {
                    return responseEnvelope.getBody().getConsultarResponse().getResponseReturn();
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