package com.rdv_service_pkfrc.service;

import com.rdv_service_pkfrc.dto.request.AjouterClientRdvRequest;
import com.rdv_service_pkfrc.dto.request.AnnulerRdvRequest;
import com.rdv_service_pkfrc.dto.request.CreerRendezVousRequest;
import com.rdv_service_pkfrc.dto.response.RendezVousResponse;


import java.util.List;

public interface RendezVousService {

    RendezVousResponse creerRendezVous(CreerRendezVousRequest request);

    RendezVousResponse ajouterClientAuRdv(String refRdv, AjouterClientRdvRequest request);

    RendezVousResponse findByRef(String refRdv);

    List<RendezVousResponse> findByResponsable(String refResponsable);

    List<RendezVousResponse> findByClient(String refClient);

    List<RendezVousResponse> findAll();

    RendezVousResponse annulerRdv(String refRdv, AnnulerRdvRequest request);
}
