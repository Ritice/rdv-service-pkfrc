package com.rdv_service_pkfrc.dto.response;

import com.rdv_service_pkfrc.entity.RendezVous;
import com.rdv_service_pkfrc.entity.enumeration.StatutRdv;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public record RendezVousResponse(
        Long id,
        String refRdv,
        ServiceResponse service,
        UtilisateurSommaireResponse responsable,
        PlageHoraireResponse plageHoraire,
        LocalDate dateRdv,
        String motifRdv,
        StatutRdv statut,
        List<UtilisateurSommaireResponse> clients,
        int nombreClients,
        LocalDateTime createdAt
) {
    public static RendezVousResponse from(RendezVous rdv) {
        List<UtilisateurSommaireResponse> clientsList = rdv.getClients().stream()
                .map(UtilisateurSommaireResponse::from)
                .toList();

        return new RendezVousResponse(
                rdv.getId(),
                rdv.getRefRdv(),
                ServiceResponse.from(rdv.getService()),
                UtilisateurSommaireResponse.from(rdv.getResponsable()),
                PlageHoraireResponse.from(rdv.getPlageHoraire()),
                rdv.getDateRdv(),
                rdv.getMotifRdv(),
                rdv.getStatut(),
                clientsList,
                clientsList.size(),
                rdv.getCreatedAt()
        );
    }
}
