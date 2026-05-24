package com.rdv_service_pkfrc.repository;

import com.rdv_service_pkfrc.entity.ServiceAdministratif;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ServiceAdministratifRepository extends JpaRepository<ServiceAdministratif, Long> {
    Optional<ServiceAdministratif> findByRef(String ref);
    Optional<ServiceAdministratif> findByRefAndActifTrue(String ref);
    boolean existsByRef(String ref);
}
