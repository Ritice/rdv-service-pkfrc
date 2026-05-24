package com.rdv_service_pkfrc.dto.response;

import com.rdv_service_pkfrc.entity.Utilisateur;
import com.rdv_service_pkfrc.entity.enumeration.RoleUtilisateur;

import java.time.LocalDateTime;



public record UtilisateurResponse(
        Long id,
        String ref,
        String email,
        String telephone,
        String nom,
        String prenom,
        String nomComplet,
        RoleUtilisateur role,
        ServiceResponse service,
        boolean actif,
        LocalDateTime createdAt
) {
    public static UtilisateurResponse from(Utilisateur u) {
        return new UtilisateurResponse(
                u.getId(), u.getRef(), u.getEmail(), u.getTelephone(),
                u.getNom(), u.getPrenom(), u.getNomComplet(), u.getRole(),
                u.getService() != null ? ServiceResponse.from(u.getService()) : null,
                u.isActif(), u.getCreatedAt()
        );
    }
}
