package com.rdv_service_pkfrc.dto.request.rdv;


import jakarta.validation.constraints.NotBlank;

public record AnnulerRdvRequestDto(
        @NotBlank(message = "Le motif d'annulation est obligatoire")
        String motif
) {}
