package com.rdv_service_pkfrc.repository;

import com.rdv_service_pkfrc.entity.Utilisateur;
import com.rdv_service_pkfrc.entity.enumeration.RoleUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByRef(String ref);

    Optional<Utilisateur> findByRefAndActifTrue(String ref);

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByRef(String ref);

    boolean existsByEmail(String email);

    List<Utilisateur> findByRoleAndActifTrue(RoleUtilisateur role);

    @Query("""
        SELECT u FROM Utilisateur u
        WHERE u.ref = :ref
          AND u.role = :role
          AND u.actif = true
    """)
    Optional<Utilisateur> findByRefAndRole(@Param("ref") String ref, @Param("role") RoleUtilisateur role);
}
