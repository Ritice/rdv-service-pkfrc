package com.rdv_service_pkfrc.controller;

import com.rdv_service_pkfrc.dto.request.AjouterClientRdvRequest;
import com.rdv_service_pkfrc.dto.request.AnnulerRdvRequest;
import com.rdv_service_pkfrc.dto.request.CreerRendezVousRequest;
import com.rdv_service_pkfrc.dto.response.ApiResponse;
import com.rdv_service_pkfrc.dto.response.RendezVousResponse;
import com.rdv_service_pkfrc.service.RendezVousService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/v1/rendez-vous")
@RequiredArgsConstructor
public class RendezVousController {

    private final RendezVousService rdvService;

    @PostMapping
    public ResponseEntity<ApiResponse<RendezVousResponse>> creer(
            @Valid @RequestBody CreerRendezVousRequest request
    ) {
        var result = rdvService.creerRendezVous(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Rendez-vous créé avec succès", result));
    }

    @GetMapping("/{refRdv}")
    public ResponseEntity<ApiResponse<RendezVousResponse>> getByRef(@PathVariable String refRdv) {
        return ResponseEntity.ok(ApiResponse.ok(rdvService.findByRef(refRdv)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RendezVousResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(rdvService.findAll()));
    }

    @GetMapping("/responsable/{refResponsable}")
    public ResponseEntity<ApiResponse<List<RendezVousResponse>>> getByResponsable(
            @PathVariable String refResponsable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(rdvService.findByResponsable(refResponsable)));
    }

    @GetMapping("/client/{refClient}")
    public ResponseEntity<ApiResponse<List<RendezVousResponse>>> getByClient(
            @PathVariable String refClient
    ) {
        return ResponseEntity.ok(ApiResponse.ok(rdvService.findByClient(refClient)));
    }

    @PostMapping("/{refRdv}/clients")
    public ResponseEntity<ApiResponse<RendezVousResponse>> ajouterClient(
            @PathVariable String refRdv,
            @Valid @RequestBody AjouterClientRdvRequest request
    ) {
        var result = rdvService.ajouterClientAuRdv(refRdv, request);
        return ResponseEntity.ok(ApiResponse.ok("Client ajouté au RDV", result));
    }

    @PatchMapping("/{refRdv}/annuler")
    public ResponseEntity<ApiResponse<RendezVousResponse>> annuler(
            @PathVariable String refRdv,
            @Valid @RequestBody AnnulerRdvRequest request
    ) {
        var result = rdvService.annulerRdv(refRdv, request);
        return ResponseEntity.ok(ApiResponse.ok("RDV annulé", result));
    }
}

