package com.rdv_service_pkfrc.service;

import com.rdv_service_pkfrc.dto.request.AjouterClientRdvRequest;
import com.rdv_service_pkfrc.dto.request.AnnulerRdvRequest;
import com.rdv_service_pkfrc.dto.request.CreerRendezVousRequest;
import com.rdv_service_pkfrc.dto.response.RendezVousResponse;
import com.rdv_service_pkfrc.entity.RendezVous;
import com.rdv_service_pkfrc.entity.Utilisateur;
import com.rdv_service_pkfrc.entity.enumeration.RoleUtilisateur;
import com.rdv_service_pkfrc.entity.enumeration.StatutRdv;
import com.rdv_service_pkfrc.exception.*;
import com.rdv_service_pkfrc.repository.PlageHoraireRepository;
import com.rdv_service_pkfrc.repository.RendezVousRepository;
import com.rdv_service_pkfrc.repository.ServiceAdministratifRepository;
import com.rdv_service_pkfrc.repository.UtilisateurRepository;
import com.rdv_service_pkfrc.service.RendezVousService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RendezVousServiceImpl implements RendezVousService {


    private static final int MAX_CLIENTS_PAR_RDV = 2;
    private static final int DELAI_MIN_JOURS = 2;

    private final RendezVousRepository rdvRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ServiceAdministratifRepository serviceRepository;
    private final PlageHoraireRepository plageRepository;

    /**
     * Crée un RDV en appliquant toutes les règles métier.
     *
     * Isolation READ_COMMITTED + verrou pessimiste sur la vérification
     * du conflit responsable pour gérer les inscriptions simultanées.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RendezVousResponse creerRendezVous(CreerRendezVousRequest req) {

        // Vérifier unicité de la référence RDV
        if (rdvRepository.existsByRefRdv(req.refRDV())) {
            throw new DuplicateReferenceException("Un RDV avec la référence '" + req.refRDV() + "' existe déjà.");
        }

        // Valider le délai minimum (2 jours)
        validerDelai(req.dateRDV());

        //Résoudre les entités liées
        var service = serviceRepository.findByRefAndActifTrue(req.refService())
                .orElseThrow(() -> ResourceNotFoundException.of("Service", req.refService()));

        var responsable = trouverResponsable(req.refResponsable());

        var plage = plageRepository.findByHeureDebut(req.heureRDV())
                .orElseThrow(() -> new BusinessException(
                        "Aucune plage horaire ne commence à " + req.heureRDV() + ". Plages valides : 08h à 15h."));

        //Vérifier le conflit responsable (avec verrou pessimiste)
        verifierConflitResponsable(responsable.getId(), plage.getId(), req.dateRDV());

        //Résoudre et valider les clients
        var clientPrincipal = trouverClient(req.refClient());
        List<Utilisateur> tousLesClients = new ArrayList<>();
        tousLesClients.add(clientPrincipal);

        if (req.refsClientsAdditionnels() != null) {
            for (String refAdd : req.refsClientsAdditionnels()) {
                if (!refAdd.equals(req.refClient())) {
                    tousLesClients.add(trouverClient(refAdd));
                }
            }
        }

        //Valider la capacité (max 2 personnes physiques)
        if (tousLesClients.size() > MAX_CLIENTS_PAR_RDV) {
            throw new CapaciteRdvException();
        }

        //Construire et persister le RDV
        var rdv = RendezVous.builder()
                .refRdv(req.refRDV())
                .service(service)
                .responsable(responsable)
                .plageHoraire(plage)
                .dateRdv(req.dateRDV())
                .motifRdv(req.motifRdv())
                .statut(StatutRdv.PLANIFIE)
                .build();

        tousLesClients.forEach(rdv::ajouterClient);

        var saved = rdvRepository.save(rdv);
        return RendezVousResponse.from(saved);
    }

    /**
     * Ajoute un client à un RDV existant (max 2 personnes au total).
     */
    @Override
    @Transactional
    public RendezVousResponse ajouterClientAuRdv(String refRdv, AjouterClientRdvRequest req) {
        var rdv = rdvRepository.findByRefRdv(refRdv)
                .orElseThrow(() -> ResourceNotFoundException.of("RDV", refRdv));

        if (rdv.getStatut() != StatutRdv.PLANIFIE) {
            throw new BusinessException("Impossible d'ajouter un client à un RDV " + rdv.getStatut().name().toLowerCase() + ".");
        }

        if (rdv.getClients().size() >= MAX_CLIENTS_PAR_RDV) {
            throw new CapaciteRdvException();
        }

        var client = trouverClient(req.refClient());

        boolean dejaPresent = rdv.getClients().stream()
                .anyMatch(c -> c.getRef().equals(req.refClient()));

        if (dejaPresent) {
            throw new BusinessException("Le client '" + req.refClient() + "' est déjà associé à ce RDV.");
        }

        rdv.ajouterClient(client);
        var saved = rdvRepository.save(rdv);
        return RendezVousResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVousResponse findByRef(String refRdv) {
        var rdv = rdvRepository.findByRefRdv(refRdv)
                .orElseThrow(() -> ResourceNotFoundException.of("RDV", refRdv));
        return RendezVousResponse.from(rdv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> findByResponsable(String refResponsable) {
        // Vérifier que le responsable existe
        if (!utilisateurRepository.existsByRef(refResponsable)) {
            throw ResourceNotFoundException.of("Responsable", refResponsable);
        }
        return rdvRepository.findByResponsableRefOrderByDateRdvAsc(refResponsable).stream()
                .map(RendezVousResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> findByClient(String refClient) {
        if (!utilisateurRepository.existsByRef(refClient)) {
            throw ResourceNotFoundException.of("Client", refClient);
        }
        return rdvRepository.findByClientRef(refClient).stream()
                .map(RendezVousResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> findAll() {
        return rdvRepository.findAll().stream()
                .map(RendezVousResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public RendezVousResponse annulerRdv(String refRdv, AnnulerRdvRequest req) {
        var rdv = rdvRepository.findByRefRdv(refRdv)
                .orElseThrow(() -> ResourceNotFoundException.of("RDV", refRdv));

        if (rdv.getStatut() == StatutRdv.ANNULE) {
            throw new BusinessException("Ce RDV est déjà annulé.");
        }
        if (rdv.getStatut() == StatutRdv.TERMINE) {
            throw new BusinessException("Impossible d'annuler un RDV terminé.");
        }

        rdv.setStatut(StatutRdv.ANNULE);
        rdv.setMotifRdv(rdv.getMotifRdv() + " [ANNULÉ : " + req.motif() + "]");
        var saved = rdvRepository.save(rdv);
        return RendezVousResponse.from(saved);
    }

    // ── Méthodes privées ─────────────────────────────────────────

    private void validerDelai(LocalDate dateRdv) {
        if (!dateRdv.isAfter(LocalDate.now().plusDays(DELAI_MIN_JOURS - 1))) {
            throw new DelaiRdvException();
        }
    }

    private Utilisateur trouverResponsable(String ref) {
        return utilisateurRepository.findByRefAndRole(ref, RoleUtilisateur.RESPONSABLE)
                .orElseThrow(() -> new BusinessException(
                        "Aucun responsable actif trouvé avec la référence : " + ref));
    }

    private Utilisateur trouverClient(String ref) {
        var utilisateur = utilisateurRepository.findByRefAndActifTrue(ref)
                .orElseThrow(() -> ResourceNotFoundException.of("Client", ref));

        if (utilisateur.getRole() != RoleUtilisateur.CLIENT) {
            throw new BusinessException("L'utilisateur '" + ref + "' n'est pas un client.");
        }
        return utilisateur;
    }

    /**
     * Vérifie si le responsable a déjà un RDV sur la même plage/date.
     * Le verrou PESSIMISTE dans le repository empêche les inscriptions
     * simultanées de créer deux RDV en conflit.
     */
    private void verifierConflitResponsable(Long responsableId, Long plageId, LocalDate dateRdv) {
        rdvRepository.findConflitResponsable(responsableId, plageId, dateRdv)
                .ifPresent(rdv -> {
                    throw new ConflitRdvException(
                            "Le responsable a déjà un RDV planifié sur cette plage horaire le " + dateRdv + ".");
                });
    }

}
