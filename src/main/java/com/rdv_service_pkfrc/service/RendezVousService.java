package com.rdv_service_pkfrc.service;

import com.rdv_service_pkfrc.dto.request.AjouterClientRdvRequest;
import com.rdv_service_pkfrc.dto.request.AnnulerRdvRequest;
import com.rdv_service_pkfrc.dto.request.CreerRendezVousRequest;
import com.rdv_service_pkfrc.dto.response.RendezVousResponse;

import java.util.List;

/**
 * Service de gestion des rendez-vous.
 * <p>
 * Fournit les opérations de création, consultation, modification
 * et annulation des rendez-vous du système.
 * </p>
 */
public interface RendezVousService {

    /**
     * Crée un nouveau rendez-vous.
     * <p>
     * Valide le délai minimum de 2 jours, vérifie l'absence de conflit
     * sur la plage horaire du responsable, et contrôle la capacité maximale
     * de 2 clients par rendez-vous.
     * </p>
     *
     * @param request les informations du rendez-vous à créer
     * @return les données du rendez-vous créé
     * @throws com.rdv_service_pkfrc.exception.DuplicateReferenceException
     *         si la référence RDV existe déjà
     * @throws com.rdv_service_pkfrc.exception.BusinessException
     *         si le délai minimum n'est pas respecté ou si la plage horaire est invalide
     * @throws com.rdv_service_pkfrc.exception.ConflitRdvException
     *         si le responsable a déjà un RDV sur cette plage et cette date
     * @throws com.rdv_service_pkfrc.exception.ResourceNotFoundException
     *         si le service, le responsable ou le client référencé n'existe pas
     */
    RendezVousResponse creerRendezVous(CreerRendezVousRequest request);

    /**
     * Ajoute un client supplémentaire à un rendez-vous existant.
     * <p>
     * La capacité maximale est de 2 clients par rendez-vous.
     * </p>
     *
     * @param refRdv    la référence unique du rendez-vous
     * @param request   les informations du client à ajouter
     * @return les données du rendez-vous mis à jour
     * @throws com.rdv_service_pkfrc.exception.ResourceNotFoundException
     *         si le rendez-vous ou le client référencé n'existe pas
     * @throws com.rdv_service_pkfrc.exception.CapaciteRdvException
     *         si le rendez-vous a déjà atteint la capacité maximale
     */
    RendezVousResponse ajouterClientAuRdv(String refRdv, AjouterClientRdvRequest request);

    /**
     * Recherche un rendez-vous par sa référence.
     *
     * @param refRdv la référence unique du rendez-vous
     * @return les données du rendez-vous trouvé
     * @throws com.rdv_service_pkfrc.exception.ResourceNotFoundException
     *         si aucun rendez-vous ne correspond à cette référence
     */
    RendezVousResponse findByRef(String refRdv);

    /**
     * Retourne la liste des rendez-vous d'un responsable.
     *
     * @param refResponsable la référence unique du responsable
     * @return la liste des rendez-vous assignés à ce responsable
     * @throws com.rdv_service_pkfrc.exception.ResourceNotFoundException
     *         si le responsable référencé n'existe pas
     */
    List<RendezVousResponse> findByResponsable(String refResponsable);

    /**
     * Retourne la liste des rendez-vous d'un client.
     *
     * @param refClient la référence unique du client
     * @return la liste des rendez-vous auxquels ce client est inscrit
     * @throws com.rdv_service_pkfrc.exception.ResourceNotFoundException
     *         si le client référencé n'existe pas
     */
    List<RendezVousResponse> findByClient(String refClient);

    /**
     * Retourne la liste de tous les rendez-vous.
     *
     * @return la liste complète des rendez-vous tous statuts confondus
     */
    List<RendezVousResponse> findAll();

    /**
     * Annule un rendez-vous existant.
     * <p>
     * Un rendez-vous déjà annulé ou terminé ne peut pas être annulé à nouveau.
     * </p>
     *
     * @param refRdv  la référence unique du rendez-vous à annuler
     * @param request les informations d'annulation (motif, etc.)
     * @return les données du rendez-vous mis à jour avec le statut {@code ANNULE}
     * @throws com.rdv_service_pkfrc.exception.ResourceNotFoundException
     *         si aucun rendez-vous ne correspond à cette référence
     * @throws com.rdv_service_pkfrc.exception.BusinessException
     *         si le rendez-vous est déjà annulé ou terminé
     */
    RendezVousResponse annulerRdv(String refRdv, AnnulerRdvRequest request);
}