package com.rdv_service_pkfrc.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AnnulerRdvRequest(
        @NotBlank(message = "Le motif d'annulation est obligatoire")
        String motif
) {}
