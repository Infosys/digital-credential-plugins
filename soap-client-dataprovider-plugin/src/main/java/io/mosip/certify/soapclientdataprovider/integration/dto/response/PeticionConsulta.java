
package io.mosip.certify.soapclientdataprovider.integration.dto.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for peticionConsulta complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="peticionConsulta"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="nuDniConsulta" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="nuDniUsuario" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="nuRucUsuario" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "peticionConsulta", propOrder = {
    "nuDniConsulta",
    "nuDniUsuario",
    "nuRucUsuario",
    "password"
})
public class PeticionConsulta {

    protected String nuDniConsulta;
    protected String nuDniUsuario;
    protected String nuRucUsuario;
    protected String password;

    /**
     * Gets the value of the nuDniConsulta property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNuDniConsulta() {
        return nuDniConsulta;
    }

    /**
     * Sets the value of the nuDniConsulta property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNuDniConsulta(String value) {
        this.nuDniConsulta = value;
    }

    /**
     * Gets the value of the nuDniUsuario property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNuDniUsuario() {
        return nuDniUsuario;
    }

    /**
     * Sets the value of the nuDniUsuario property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNuDniUsuario(String value) {
        this.nuDniUsuario = value;
    }

    /**
     * Gets the value of the nuRucUsuario property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNuRucUsuario() {
        return nuRucUsuario;
    }

    /**
     * Sets the value of the nuRucUsuario property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNuRucUsuario(String value) {
        this.nuRucUsuario = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

}
