package io.mosip.certify.peruiddataprovider.integration.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.mosip.certify.peruiddataprovider.integration.dto.request.ConsultaArg;
import io.mosip.certify.peruiddataprovider.integration.dto.response.ResponseEnvelope;
import io.mosip.certify.peruiddataprovider.integration.dto.response.ResponseReturn;
import io.mosip.certify.util.SoapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class ConsultaDniService {
    final String ENDPOINT_URI = "http://65.1.93.129/consultadnie/ConsultaDniService";

    public ResponseReturn getConsultarResponse(ConsultaArg consultaArg) throws Exception {
        try {
            // SOAP Request Body
            String soapRequest = SoapUtil.buildSoapRequest(consultaArg);
            // Send SOAP request and get response
            String soapResponse = sendSOAPRequest(ENDPOINT_URI, soapRequest);

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

    private String sendSOAPRequest(String endpointUrl, String soapRequest) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setDoOutput(true);
        // Send SOAP request
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(soapRequest.getBytes());
            outputStream.flush();
        }
        // Read the response
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } else {
            throw new RuntimeException("HTTP error code: " + connection.getResponseCode());
        }
    }

    private String createSoapRequest() {
        String header = "<soapenv:Header/>";

        String nuDniConsulta = "<nuDniConsulta>06794000</nuDniConsulta>";
        String nuDniUsuario = "<nuDniUsuario>06794000</nuDniUsuario>";
        String nuRucUsuario = "<nuRucUsuario>20295613620</nuRucUsuario>";
        String password = "<password>06794000</password>";

        String arg0 = "<arg0>" + nuDniConsulta + nuDniUsuario + nuRucUsuario + password + "</arg0>";
        String consultar = "<end:consultar>" + arg0 + "</end:consultar>";
        String body = "<soapenv:Body>" + consultar + "</soapenv:Body>";
        String envelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:end=\"http://endpoint.wsconsultadni.reniec.gob.pe/\">";

        return envelope + header + body + "</soapenv:Envelope>";
    }
}