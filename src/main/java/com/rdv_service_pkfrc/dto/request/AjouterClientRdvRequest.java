package com.rdv_service_pkfrc.dto.request;

import jakarta.validation.constraints.NotBlank;


public record AjouterClientRdvRequest(
        @NotBlank(message = "La référence client est obligatoire")
        String refClient
) {}
