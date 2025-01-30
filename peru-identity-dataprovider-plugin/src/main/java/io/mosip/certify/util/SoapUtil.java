package io.mosip.certify.util;

import io.mosip.certify.peruiddataprovider.integration.dto.request.ConsultaArg;
import io.mosip.certify.peruiddataprovider.integration.dto.request.Consultar;
import io.mosip.certify.peruiddataprovider.integration.dto.request.RequestBody;
import io.mosip.certify.peruiddataprovider.integration.dto.request.RequestEnvelope;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SoapUtil {
    public static String buildSoapRequest(ConsultaArg consultaArg) throws Exception {
        // Create the object structure

        Consultar consultar = new Consultar();
        consultar.setArg0(consultaArg);

        RequestBody body = new RequestBody();
        body.setConsultar(consultar);

        RequestEnvelope envelope = new RequestEnvelope();
        envelope.setBody(body);

        // Convert to XML
        JAXBContext context = JAXBContext.newInstance(RequestEnvelope.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter sw = new StringWriter();
        marshaller.marshal(envelope, sw);
        return sw.toString();
    }

    public static String sendSOAPRequest(String endpointUrl, String soapRequest) throws Exception {
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
}
