package io.mosip.certify.peruiddataprovider.integration.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "consultarResponse", namespace = "http://endpoint.wsconsultadni.reniec.gob.pe/")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsultarResponse {
    @JacksonXmlProperty(localName = "return")
    private ResponseReturn responseReturn;

    public ResponseReturn getResponseReturn() { return responseReturn; }
    public void setResponseReturn(ResponseReturn responseReturn) { this.responseReturn = responseReturn; }
}