package com.rdv_service_pkfrc.dto.response;


import com.rdv_service_pkfrc.entity.Utilisateur;
import com.rdv_service_pkfrc.entity.enumeration.RoleUtilisateur;

public record UtilisateurSommaireResponse(
        String ref,
        String nomComplet,
        String email,
        RoleUtilisateur role
) {
    public static UtilisateurSommaireResponse from(Utilisateur u) {
        return new UtilisateurSommaireResponse(u.getRef(), u.getNomComplet(), u.getEmail(), u.getRole());
    }
}
