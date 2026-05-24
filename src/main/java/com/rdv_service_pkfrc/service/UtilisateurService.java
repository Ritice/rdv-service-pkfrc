package com.rdv_service_pkfrc.service;

import com.rdv_service_pkfrc.dto.request.CreerClientRequest;
import com.rdv_service_pkfrc.dto.request.CreerResponsableRequest;
import com.rdv_service_pkfrc.dto.response.UtilisateurResponse;
import com.rdv_service_pkfrc.entity.enumeration.RoleUtilisateur;

import java.util.List;

/**
 * Service de gestion des utilisateurs (clients et responsables).
 * <p>
 * Fournit les opérations de création, consultation et désactivation
 * des utilisateurs du système de rendez-vous.
 * </p>
 */
public interface UtilisateurService {

    /**
     * Crée un nouveau client.
     *
     * @param request les informations du client à créer
     * @return les données du client créé
     * @throws com.rdv_service_pkfrc.exception.DuplicateReferenceException
     *         si la référence ou l'email existe déjà
     */
    UtilisateurResponse creerClient(CreerClientRequest request);

    /**
     * Crée un nouveau responsable et l'affecte à un service administratif.
     *
     * @param request les informations du responsable à créer
     * @return les données du responsable créé
     * @throws com.rdv_service_pkfrc.exception.DuplicateReferenceException
     *         si la référence ou l'email existe déjà
     * @throws com.rdv_service_pkfrc.exception.ResourceNotFoundException
     *         si le service administratif référencé n'existe pas
     */
    UtilisateurResponse creerResponsable(CreerResponsableRequest request);

    /**
     * Recherche un utilisateur par sa référence.
     *
     * @param ref la référence unique de l'utilisateur
     * @return les données de l'utilisateur trouvé
     * @throws com.rdv_service_pkfrc.exception.ResourceNotFoundException
     *         si aucun utilisateur ne correspond à cette référence
     */
    UtilisateurResponse findByRef(String ref);

    /**
     * Retourne la liste de tous les utilisateurs actifs et inactifs.
     *
     * @return la liste complète des utilisateurs
     */
    List<UtilisateurResponse> findAll();

    /**
     * Retourne la liste des utilisateurs filtrés par rôle.
     *
     * @param role le rôle à filtrer ({@link RoleUtilisateur#CLIENT} ou
     *             {@link RoleUtilisateur#RESPONSABLE})
     * @return la liste des utilisateurs ayant ce rôle
     */
    List<UtilisateurResponse> findByRole(RoleUtilisateur role);

    /**
     * Désactive un utilisateur (soft delete).
     * <p>
     * L'utilisateur n'est pas supprimé de la base de données mais
     * son statut {@code actif} est mis à {@code false}.
     * </p>
     *
     * @param ref la référence unique de l'utilisateur à désactiver
     * @throws com.rdv_service_pkfrc.exception.ResourceNotFoundException
     *         si aucun utilisateur ne correspond à cette référence
     */
    void desactiverUtilisateur(String ref);
}