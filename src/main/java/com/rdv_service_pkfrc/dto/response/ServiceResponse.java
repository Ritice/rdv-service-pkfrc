package com.rdv_service_pkfrc.dto.response;


import com.rdv_service_pkfrc.entity.ServiceAdministratif;

public record ServiceResponse(
        Long id,
        String ref,
        String libelle,
        boolean actif
) {
    public static ServiceResponse from(ServiceAdministratif s) {
        return new ServiceResponse(s.getId(), s.getRef(), s.getLibelle(), s.isActif());
    }
}