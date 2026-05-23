package com.rdv_service_pkfrc.dto.request.client;

import jakarta.validation.constraints.*;


public record ClientResquestDto(

        @NotBlank(message = "La référence est obligatoire")
        @Size(max = 50)
        String ref,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format email invalide")
        String email,

        @NotBlank(message = "Le téléphone est obligatoire")
        String telephone,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100)
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100)
        String prenom
) {}
