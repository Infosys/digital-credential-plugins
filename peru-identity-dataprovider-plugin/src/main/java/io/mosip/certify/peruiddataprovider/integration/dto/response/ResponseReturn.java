package io.mosip.certify.peruiddataprovider.integration.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.mosip.certify.peruiddataprovider.integration.dto.response.DatosPersona;

@JacksonXmlRootElement(localName = "return")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseReturn {
    @JacksonXmlProperty(localName = "coResultado")
    private String coResultado;

    @JacksonXmlProperty(localName = "deResultado")
    private String deResultado;

    @JacksonXmlProperty(localName = "datosPersona")
    private DatosPersona datosPersona;

    public DatosPersona getDatosPersona() {
        return datosPersona;
    }

    public void setDatosPersona(DatosPersona datosPersona) {
        this.datosPersona = datosPersona;
    }

    public String getCoResultado() { return coResultado; }
    public void setCoResultado(String coResultado) { this.coResultado = coResultado; }

    public String getDeResultado() { return deResultado; }
    public void setDeResultado(String deResultado) { this.deResultado = deResultado; }
}