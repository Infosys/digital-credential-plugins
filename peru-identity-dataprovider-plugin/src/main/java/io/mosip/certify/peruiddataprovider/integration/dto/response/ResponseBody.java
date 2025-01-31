package io.mosip.certify.peruiddataprovider.integration.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "Body")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseBody {
    @JacksonXmlProperty(localName = "consultarResponse", namespace = "http://endpoint.wsconsultadni.reniec.gob.pe/")
    private ConsultarResponse consultarResponse;

    public ConsultarResponse getConsultarResponse() { return consultarResponse; }
    public void setConsultarResponse(ConsultarResponse consultarResponse) { this.consultarResponse = consultarResponse; }
}