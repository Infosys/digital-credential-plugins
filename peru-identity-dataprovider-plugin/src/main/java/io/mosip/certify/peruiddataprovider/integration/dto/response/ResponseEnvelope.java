package io.mosip.certify.peruiddataprovider.integration.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.mosip.certify.peruiddataprovider.integration.dto.response.ResponseBody;

@JacksonXmlRootElement(localName = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseEnvelope {

    @JacksonXmlProperty(localName = "Body")
    private ResponseBody body;

    public ResponseBody getBody() { return body; }
    public void setBody(ResponseBody body) { this.body = body; }
}