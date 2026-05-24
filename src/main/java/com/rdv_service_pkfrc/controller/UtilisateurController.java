package com.rdv_service_pkfrc.controller;

import com.rdv_service_pkfrc.dto.request.CreerClientRequest;
import com.rdv_service_pkfrc.dto.request.CreerResponsableRequest;
import com.rdv_service_pkfrc.dto.response.ApiResponse;
import com.rdv_service_pkfrc.dto.response.UtilisateurResponse;
import com.rdv_service_pkfrc.entity.enumeration.RoleUtilisateur;
import com.rdv_service_pkfrc.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    //CLIENT
    @PostMapping("/clients")
    public ResponseEntity<ApiResponse<UtilisateurResponse>> creerClient(
            @Valid @RequestBody CreerClientRequest request
    ) {
        var result = utilisateurService.creerClient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Client créé avec succès", result));
    }

    //RESPONSABLE

    @PostMapping("/responsables")
    public ResponseEntity<ApiResponse<UtilisateurResponse>> creerResponsable(
            @Valid @RequestBody CreerResponsableRequest request
    ) {
        var result = utilisateurService.creerResponsable(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Responsable créé avec succès", result));
    }

    // CONSULTATION

    @GetMapping("/{ref}")
    public ResponseEntity<ApiResponse<UtilisateurResponse>> getByRef(@PathVariable String ref) {
        return ResponseEntity.ok(ApiResponse.ok(utilisateurService.findByRef(ref)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UtilisateurResponse>>> getAll(
            @RequestParam(required = false) RoleUtilisateur role
    ) {
        var result = role != null
                ? utilisateurService.findByRole(role)
                : utilisateurService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    //DESACTIVATION
    @DeleteMapping("/{ref}")
    public ResponseEntity<ApiResponse<Void>> desactiver(@PathVariable String ref) {
        utilisateurService.desactiverUtilisateur(ref);
        return ResponseEntity.ok(ApiResponse.ok("Utilisateur désactivé", null));
    }
}

