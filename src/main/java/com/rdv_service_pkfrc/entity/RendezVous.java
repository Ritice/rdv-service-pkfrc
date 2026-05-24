package com.rdv_service_pkfrc.entity;


import com.rdv_service_pkfrc.entity.enumeration.StatutRdv;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
name = "rendez_vous",
uniqueConstraints = @UniqueConstraint(
        name = "uq_responsable_plage_date",
        columnNames = {"ref_responsable", "ref_plage", "date_rdv"}
)
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVous extends AudiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_rdv", nullable = false, unique = true, length = 50)
    private String refRdv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ref_service", nullable = false)
    private ServiceAdministratif service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ref_responsable", nullable = false)
    private Utilisateur responsable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ref_plage", nullable = false)
    private PlageHoraire plageHoraire;

    @Column(name = "date_rdv", nullable = false)
    private LocalDate dateRdv;

    @Column(name = "motif_rdv", nullable = false)
    private String motifRdv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutRdv statut = StatutRdv.PLANIFIE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Optimistic locking pour gérer la concurrence
    @Version
    private Long version;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rdv_client",
            joinColumns = @JoinColumn(name = "rdv_id"),
            inverseJoinColumns = @JoinColumn(name = "client_id")
    )
    @Builder.Default
    private Set<Utilisateur> clients = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (statut == null) statut = StatutRdv.PLANIFIE;
    }

    public void ajouterClient(Utilisateur client) {
        clients.add(client);
    }
}

