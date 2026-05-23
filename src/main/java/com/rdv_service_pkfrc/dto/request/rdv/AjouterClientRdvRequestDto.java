package com.rdv_service_pkfrc.dto.request.rdv;

import jakarta.validation.constraints.NotBlank;


public record AjouterClientRdvRequestDto(
        @NotBlank(message = "La référence client est obligatoire")
        String refClient
) {}
