package com.rdv_service_pkfrc.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "service_administratif")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceAdministratif extends AudiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String ref;

    @Column(nullable = false, unique = true, length = 100)
    private String libelle;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;
}

