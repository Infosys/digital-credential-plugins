package io.mosip.certify.soapclientdataprovider.integration.service;

import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.api.spi.DataProviderPlugin;
import io.mosip.certify.soapclientdataprovider.integration.dto.DatosPersona;
import io.mosip.certify.soapclientdataprovider.integration.dto.ResponseReturn;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConditionalOnProperty(value = "mosip.certify.integration.data-provider-plugin", havingValue = "SoapClientDataProviderPlugin")
@Component
@Slf4j
public class SoapClientDataProviderPlugin implements DataProviderPlugin {
    @Autowired
    private ConsultaDniService consultaDniService;


    @Override
    public JSONObject fetchData(Map<String, Object> identityDetails) throws DataProviderExchangeException {
        JSONObject jsonObject = new JSONObject();
        try {
            ResponseReturn responseReturn = consultaDniService.getConsultarResponse();
            log.info("co result: " + responseReturn.getCoResultado());
            log.info("de result: " + responseReturn.getDeResultado());
            if(!responseReturn.getCoResultado().equals("0000")) {
                throw new Exception("INVALID_DNI");
            }

            if(responseReturn.getDatosPersona() != null) {
                DatosPersona datosPersona = responseReturn.getDatosPersona();
                jsonObject.put("dni", datosPersona.getDni());
                jsonObject.put("prenombres", datosPersona.getPrenombres());
                jsonObject.put("primerApellido", datosPersona.getPrimerApellido());
                jsonObject.put("apellidoCasada", datosPersona.getApellidoCasada());
                jsonObject.put("segundoApellido", datosPersona.getSegundoApellido());
                jsonObject.put("fechaNacimiento", datosPersona.getFechaNacimiento());
                jsonObject.put("genero", datosPersona.getGenero());
                jsonObject.put("estadoCivil", datosPersona.getEstadoCivil());
                jsonObject.put("restriccion", datosPersona.getRestriccion());

                return jsonObject;
            }
        } catch (Exception e) {
            log.error("Failed to fetch response from soap resource.");
            throw new DataProviderExchangeException("INVALID_CONSULTA_DNI");
        }
        throw new DataProviderExchangeException("FAILED_TO_FETCH_DATA");
    }
}
