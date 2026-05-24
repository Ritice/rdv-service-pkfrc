package com.rdv_service_pkfrc.service;

import com.rdv_service_pkfrc.dto.request.CreerClientRequest;
import com.rdv_service_pkfrc.dto.request.CreerResponsableRequest;
import com.rdv_service_pkfrc.dto.response.UtilisateurResponse;
import com.rdv_service_pkfrc.entity.enumeration.RoleUtilisateur;

import java.util.List;

public interface UtilisateurService {

    UtilisateurResponse creerClient(CreerClientRequest request);

    UtilisateurResponse creerResponsable(CreerResponsableRequest request);

    UtilisateurResponse findByRef(String ref);

    List<UtilisateurResponse> findAll();

    List<UtilisateurResponse> findByRole(RoleUtilisateur role);

    void desactiverUtilisateur(String ref);
}
