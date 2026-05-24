package com.rdv_service_pkfrc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CreerRendezVousRequest(
        @NotBlank(message = "La référence client est obligatoire")
        String refClient,

        @NotBlank(message = "La référence RDV est obligatoire")
        @Size(max = 50)
        String refRDV,

        @NotBlank(message = "La référence service est obligatoire")
        String refService,

        @NotBlank(message = "La référence responsable est obligatoire")
        String refResponsable,

        @NotNull(message = "La date du RDV est obligatoire")
        LocalDate dateRDV,

        @NotNull(message = "L'heure du RDV est obligatoire")
        LocalTime heureRDV,

        @NotBlank(message = "Le motif du RDV est obligatoire")
        @Size(max = 1000)
        String motifRdv,

        // Clients additionnels (optionnel, max 1 car le premier vient de refClient)
        List<@NotBlank String> refsClientsAdditionnels
) {}

