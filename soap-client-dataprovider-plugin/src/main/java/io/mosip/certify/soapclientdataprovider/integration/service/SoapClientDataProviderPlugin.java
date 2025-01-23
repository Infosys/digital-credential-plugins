package io.mosip.certify.soapclientdataprovider.integration.service;

import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.api.spi.DataProviderPlugin;
import io.mosip.certify.gen.DatosPersona;
import io.mosip.certify.gen.ResultadoConsulta;
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
            String nuDniConsulta = (String) identityDetails.get("nuDniConsulta");
            String nuDniUsuario = (String) identityDetails.get("nuDniUsuario");
            String nuRucUsuario = (String) identityDetails.get("nuRucUsuario");
            String password = (String) identityDetails.get("password");
            ResultadoConsulta consultResult = consultaDniService.consultarDni(nuDniConsulta, nuDniUsuario, nuRucUsuario, password);

            if(consultResult.getDatosPersona() != null) {
                DatosPersona datosPersona = consultResult.getDatosPersona();
                jsonObject.put("dni", datosPersona.getDni());
                jsonObject.put("prenombres", datosPersona);
                jsonObject.put("primerApellido", datosPersona);
                jsonObject.put("apellidoCasada", datosPersona);
                jsonObject.put("segundoApellido", datosPersona);
                jsonObject.put("fechaNacimiento", datosPersona);
                jsonObject.put("genero", datosPersona);
                jsonObject.put("estadoCivil", datosPersona);
                jsonObject.put("restriccion", datosPersona);

                return jsonObject;
            }
        } catch (Exception e) {
            log.error("Failed to fetch response from soap resource.");
            throw new DataProviderExchangeException("ERROR_FETCHING_DATA_FROM_SOAP_RESOURCE");
        }
        throw new DataProviderExchangeException("FAILED_TO_FETCH_DATA");
    }
}
