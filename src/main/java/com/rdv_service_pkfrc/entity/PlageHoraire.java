package com.rdv_service_pkfrc.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "plage_horaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlageHoraire{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "heure_debut", nullable = false, unique = true)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Column(nullable = false, length = 20)
    private String libelle;
}
