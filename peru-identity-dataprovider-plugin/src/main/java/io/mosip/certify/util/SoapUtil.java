package io.mosip.certify.util;

import io.mosip.certify.peruiddataprovider.integration.dto.request.ConsultaArg;
import io.mosip.certify.peruiddataprovider.integration.dto.request.Consultar;
import io.mosip.certify.peruiddataprovider.integration.dto.request.RequestBody;
import io.mosip.certify.peruiddataprovider.integration.dto.request.RequestEnvelope;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import java.io.StringWriter;

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
}
