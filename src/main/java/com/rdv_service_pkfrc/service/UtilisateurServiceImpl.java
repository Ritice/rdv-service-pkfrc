package com.rdv_service_pkfrc.service;

import com.rdv_service_pkfrc.dto.request.CreerClientRequest;
import com.rdv_service_pkfrc.dto.request.CreerResponsableRequest;
import com.rdv_service_pkfrc.dto.response.UtilisateurResponse;
import com.rdv_service_pkfrc.entity.Utilisateur;
import com.rdv_service_pkfrc.entity.enumeration.RoleUtilisateur;
import com.rdv_service_pkfrc.exception.DuplicateReferenceException;
import com.rdv_service_pkfrc.exception.ResourceNotFoundException;
import com.rdv_service_pkfrc.repository.ServiceAdministratifRepository;
import com.rdv_service_pkfrc.repository.UtilisateurRepository;
import com.rdv_service_pkfrc.service.UtilisateurService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {


    private final UtilisateurRepository utilisateurRepository;
    private final ServiceAdministratifRepository serviceRepository;

    @Override
    @Transactional
    public UtilisateurResponse creerClient(CreerClientRequest req) {
        validerReferenceUnique(req.ref());
        validerEmailUnique(req.email());

        var client = Utilisateur.builder()
                .ref(req.ref())
                .email(req.email())
                .telephone(req.telephone())
                .nom(req.nom())
                .prenom(req.prenom())
                .role(RoleUtilisateur.CLIENT)
                .build();

        var saved = utilisateurRepository.save(client);
        return UtilisateurResponse.from(saved);
    }

    @Override
    @Transactional
    public UtilisateurResponse creerResponsable(CreerResponsableRequest req) {
        validerReferenceUnique(req.ref());
        validerEmailUnique(req.email());

        var service = serviceRepository.findByRefAndActifTrue(req.refService())
                .orElseThrow(() -> ResourceNotFoundException.of("Service", req.refService()));

        var responsable = Utilisateur.builder()
                .ref(req.ref())
                .email(req.email())
                .telephone(req.telephone())
                .nom(req.nom())
                .prenom(req.prenom())
                .role(RoleUtilisateur.RESPONSABLE)
                .service(service)
                .build();

        var saved = utilisateurRepository.save(responsable);
        return UtilisateurResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponse findByRef(String ref) {
        var utilisateur = utilisateurRepository.findByRef(ref)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", ref));
        return UtilisateurResponse.from(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> findAll() {
        return utilisateurRepository.findAll().stream()
                .map(UtilisateurResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> findByRole(RoleUtilisateur role) {
        return utilisateurRepository.findByRoleAndActifTrue(role).stream()
                .map(UtilisateurResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void desactiverUtilisateur(String ref) {
        var utilisateur = utilisateurRepository.findByRef(ref)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", ref));
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
    }

    //Méthodes privées
    private void validerReferenceUnique(String ref) {
        if (utilisateurRepository.existsByRef(ref)) {
            throw new DuplicateReferenceException("Un utilisateur avec la référence '" + ref + "' existe déjà.");
        }
    }

    private void validerEmailUnique(String email) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new DuplicateReferenceException("Un utilisateur avec l'email '" + email + "' existe déjà.");
        }
    }
}
