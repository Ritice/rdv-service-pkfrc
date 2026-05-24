package com.rdv_service_pkfrc.repository;

import com.rdv_service_pkfrc.entity.PlageHoraire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;



public interface PlageHoraireRepository extends JpaRepository<PlageHoraire, Long> {
    Optional<PlageHoraire> findByHeureDebut(LocalTime heureDebut);
    List<PlageHoraire> findAllByOrderByHeureDebutAsc();
}
