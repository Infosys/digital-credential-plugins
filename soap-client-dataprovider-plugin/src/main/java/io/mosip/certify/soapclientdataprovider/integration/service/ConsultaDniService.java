package io.mosip.certify.soapclientdataprovider.integration.service;

import io.mosip.certify.gen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsultaDniService {

    private final SRELServiceService srelServiceService;

    @Autowired
    public ConsultaDniService(SRELServiceService srelServiceService) {
        this.srelServiceService = srelServiceService;
    }

    public ResultadoConsulta consultarDni(String nuDniConsulta, String nuDniUsuario, String nuRucUsuario, String password) {
        PeticionConsulta peticion = new PeticionConsulta();
        peticion.setNuDniConsulta(nuDniConsulta);
        peticion.setNuDniUsuario(nuDniUsuario);
        peticion.setNuRucUsuario(nuRucUsuario);
        peticion.setPassword(password);

        return srelServiceService.consultar(peticion);
    }

    public ResultadoConsulta consultarCertificado(String accessToken, String nuDniConsulta, String nuRucUsuario) {
        PeticionConsultaCertificado peticion = new PeticionConsultaCertificado();
        peticion.setAccessToken(accessToken);
        peticion.setNuDniConsulta(nuDniConsulta);
        peticion.setNuRucUsuario(nuRucUsuario);

        return srelServiceService.consultarCertificado(peticion);
    }

    public ResultadoActualizacionCredencial actualizarCredencial(String credencialAnterior, String credencialNueva,
                                                                 String nuDni, String nuRuc) {
        PeticionActualizarCredencial peticion = new PeticionActualizarCredencial();
        peticion.setCredencialAnterior(credencialAnterior);
        peticion.setCredencialNueva(credencialNueva);
        peticion.setNuDni(nuDni);
        peticion.setNuRuc(nuRuc);

        return srelServiceService.actualizarCredencial(peticion);
    }
}