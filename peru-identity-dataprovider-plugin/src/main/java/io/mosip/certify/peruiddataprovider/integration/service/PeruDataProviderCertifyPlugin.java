package io.mosip.certify.peruiddataprovider.integration.service;

import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.api.spi.DataProviderPlugin;
import io.mosip.certify.peruiddataprovider.integration.dto.request.ConsultaArg;
import io.mosip.certify.peruiddataprovider.integration.dto.response.DatosPersona;
import io.mosip.certify.peruiddataprovider.integration.dto.response.ResponseReturn;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConditionalOnProperty(value = "mosip.certify.integration.data-provider-plugin", havingValue = "SoapClientDataProviderPlugin")
@Component
@Slf4j
public class PeruDataProviderCertifyPlugin implements DataProviderPlugin {
    @Autowired
    private ConsultaDniService consultaDniService;

    @Value("${mosip.certify.peru-id.data-provider-plugin.nu-dni-usuario}")
    private String nuDniUsuario;

    @Value("${mosip.certify.peru-id.data-provider-plugin.nu-ruc-usuario}")
    private String nuRucUsuario;

    @Value("${mosip.certify.peru-id.data-provider-plugin.password}")
    private String password;


    @Override
    public JSONObject fetchData(Map<String, Object> identityDetails) throws DataProviderExchangeException {
        try {
            String nuConsultaDni = (String) identityDetails.get("sub");
            ConsultaArg arg = new ConsultaArg();
            arg.setNuDniConsulta(nuConsultaDni);
            arg.setNuDniUsuario(nuDniUsuario);
            arg.setNuRucUsuario(nuRucUsuario);
            arg.setPassword(password);
            JSONObject jsonObject = new JSONObject();
            ResponseReturn responseReturn = consultaDniService.getConsultarResponse(arg);
            log.info("co result: " + responseReturn.getCoResultado());
            log.info("de result: " + responseReturn.getDeResultado());
            if(!responseReturn.getCoResultado().equals("0000")) {
                throw new Exception(responseReturn.getDeResultado());
            }

            if(responseReturn.getDatosPersona() != null) {
                DatosPersona datosPersona = responseReturn.getDatosPersona();
                jsonObject.put("dni", datosPersona.getDni());
                jsonObject.put("firstName", datosPersona.getPrenombres());
                jsonObject.put("firstLastName", datosPersona.getPrimerApellido());
                jsonObject.put("secondLastName", datosPersona.getSegundoApellido());
                jsonObject.put("dateOfBirth", datosPersona.getFechaNacimiento());
                jsonObject.put("gender", datosPersona.getGenero());
                jsonObject.put("maritalStatus", datosPersona.getEstadoCivil());
                jsonObject.put("restriction", datosPersona.getRestriccion());
                jsonObject.put("face", datosPersona.getFoto());
                return jsonObject;
            }
        } catch (Exception e) {
            log.error("Failed to fetch response from soap resource.");
            throw new DataProviderExchangeException(e.getMessage());
        }
        throw new DataProviderExchangeException("FAILED_TO_FETCH_DATA");
    }
}
