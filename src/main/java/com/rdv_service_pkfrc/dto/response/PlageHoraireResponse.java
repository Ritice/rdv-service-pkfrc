package com.rdv_service_pkfrc.dto.response;

import com.rdv_service_pkfrc.entity.PlageHoraire;

import java.time.LocalTime;


public record PlageHoraireResponse(
        Long id,
        LocalTime heureDebut,
        LocalTime heureFin,
        String libelle
) {
    public static PlageHoraireResponse from(PlageHoraire p) {
        return new PlageHoraireResponse(p.getId(), p.getHeureDebut(), p.getHeureFin(), p.getLibelle());
    }
}
