package com.rdv_service_pkfrc.controller;

import com.rdv_service_pkfrc.dto.response.ApiResponse;
import com.rdv_service_pkfrc.dto.response.PlageHoraireResponse;
import com.rdv_service_pkfrc.dto.response.ServiceResponse;
import com.rdv_service_pkfrc.repository.PlageHoraireRepository;
import com.rdv_service_pkfrc.repository.ServiceAdministratifRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
public class ReferentielController {

    private final ServiceAdministratifRepository serviceRepository;
    private final PlageHoraireRepository plageRepository;

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getServices() {
        var services = serviceRepository.findAll().stream()
                .map(ServiceResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(services));
    }

    @GetMapping("/plages")
    public ResponseEntity<ApiResponse<List<PlageHoraireResponse>>> getPlages() {
        var plages = plageRepository.findAllByOrderByHeureDebutAsc().stream()
                .map(PlageHoraireResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(plages));
    }
}
