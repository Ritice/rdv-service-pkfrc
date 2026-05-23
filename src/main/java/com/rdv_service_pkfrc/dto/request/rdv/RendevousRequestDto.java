package com.rdv_service_pkfrc.dto.request.rdv;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RendevousRequestDto(

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

        List<@NotBlank String> refsClientsAdditionnels
) {}
