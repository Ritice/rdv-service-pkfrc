package com.rdv_service_pkfrc.repository;

import com.rdv_service_pkfrc.entity.RendezVous;
import com.rdv_service_pkfrc.entity.enumeration.StatutRdv;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;



public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    Optional<RendezVous> findByRefRdv(String refRdv);

    /**
     * Vérifie si un responsable a déjà un RDV sur une plage/date donnée.
     * Utilise un verrou PESSIMISTE pour éviter les race conditions
     * lors de créations simultanées.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r FROM RendezVous r
        WHERE r.responsable.id = :responsableId
          AND r.plageHoraire.id = :plageId
          AND r.dateRdv = :dateRdv
          AND r.statut != 'ANNULE'
    """)
    Optional<RendezVous> findConflitResponsable(
            @Param("responsableId") Long responsableId,
            @Param("plageId") Long plageId,
            @Param("dateRdv") LocalDate dateRdv
    );

    /**
     * Compte le nombre de clients sur un RDV (pour la limite de 2 personnes).
     */
    @Query("SELECT SIZE(r.clients) FROM RendezVous r WHERE r.id = :rdvId")
    int countClients(@Param("rdvId") Long rdvId);

    List<RendezVous> findByServiceRefAndDateRdvAndStatutNot(
            String serviceRef, LocalDate dateRdv, StatutRdv statut
    );

    List<RendezVous> findByResponsableRefOrderByDateRdvAsc(String responsableRef);

    @Query("""
        SELECT r FROM RendezVous r
        JOIN r.clients c
        WHERE c.ref = :clientRef
        ORDER BY r.dateRdv ASC
    """)
    List<RendezVous> findByClientRef(@Param("clientRef") String clientRef);

    boolean existsByRefRdv(String refRdv);
}
