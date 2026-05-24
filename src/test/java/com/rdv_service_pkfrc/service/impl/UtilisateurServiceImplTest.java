package com.rdv_service_pkfrc.service.impl;

import com.rdv_service_pkfrc.dto.request.CreerClientRequest;
import com.rdv_service_pkfrc.dto.request.CreerResponsableRequest;
import com.rdv_service_pkfrc.dto.response.UtilisateurResponse;
import com.rdv_service_pkfrc.entity.ServiceAdministratif;
import com.rdv_service_pkfrc.entity.Utilisateur;
import com.rdv_service_pkfrc.entity.enumeration.RoleUtilisateur;
import com.rdv_service_pkfrc.exception.DuplicateReferenceException;
import com.rdv_service_pkfrc.exception.ResourceNotFoundException;
import com.rdv_service_pkfrc.repository.ServiceAdministratifRepository;
import com.rdv_service_pkfrc.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UtilisateurServiceImpl — Tests unitaires")
class UtilisateurServiceImplTest {

    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private ServiceAdministratifRepository serviceRepository;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    private ServiceAdministratif service;

    @BeforeEach
    void setUp() {
        service = ServiceAdministratif.builder()
                .id(1L).ref("SVC-001").libelle("Archives").actif(true).build();
    }

    // scenario de test pour la methode creerClient

    @Nested
    @DisplayName("creerClient()")
    class CreerClient {

        private CreerClientRequest buildRequest() {
            return new CreerClientRequest(
                    "CLI-001", "alice.martin@test.com",
                    "0600000001", "Martin", "Alice"
            );
        }

        @Test
        @DisplayName("Doit créer un client avec succès")
        void shouldCreateClientSuccessfully() {
            when(utilisateurRepository.existsByRef("CLI-001")).thenReturn(false);
            when(utilisateurRepository.existsByEmail("alice.martin@test.com")).thenReturn(false);

            var saved = Utilisateur.builder()
                    .id(1L).ref("CLI-001").email("alice.martin@test.com")
                    .nom("Martin").prenom("Alice").telephone("0600000001")
                    .role(RoleUtilisateur.CLIENT).actif(true).build();

            when(utilisateurRepository.save(any())).thenReturn(saved);

            UtilisateurResponse result = utilisateurService.creerClient(buildRequest());

            assertThat(result).isNotNull();
            assertThat(result.ref()).isEqualTo("CLI-001");
            assertThat(result.role()).isEqualTo(RoleUtilisateur.CLIENT);
            verify(utilisateurRepository).save(any(Utilisateur.class));
        }

        @Test
        @DisplayName("Doit lever DuplicateReferenceException si la référence existe déjà")
        void shouldThrowWhenRefAlreadyExists() {
            when(utilisateurRepository.existsByRef("CLI-001")).thenReturn(true);

            assertThatThrownBy(() -> utilisateurService.creerClient(buildRequest()))
                    .isInstanceOf(DuplicateReferenceException.class)
                    .hasMessageContaining("CLI-001");

            verify(utilisateurRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever DuplicateReferenceException si l'email existe déjà")
        void shouldThrowWhenEmailAlreadyExists() {
            when(utilisateurRepository.existsByRef("CLI-001")).thenReturn(false);
            when(utilisateurRepository.existsByEmail("alice.martin@test.com")).thenReturn(true);

            assertThatThrownBy(() -> utilisateurService.creerClient(buildRequest()))
                    .isInstanceOf(DuplicateReferenceException.class)
                    .hasMessageContaining("alice.martin@test.com");

            verify(utilisateurRepository, never()).save(any());
        }
    }

    //  scenario de test pour la methode creerResponsable

    @Nested
    @DisplayName("creerResponsable()")
    class CreerResponsable {

        private CreerResponsableRequest buildRequest() {
            return new CreerResponsableRequest(
                    "RESP-001", "jean.dupont@test.com",
                    "0600000002", "Dupont", "Jean", "SVC-001"
            );
        }

        @Test
        @DisplayName("Doit créer un responsable et l'affecter au bon service")
        void shouldCreateResponsableSuccessfully() {
            when(utilisateurRepository.existsByRef("RESP-001")).thenReturn(false);
            when(utilisateurRepository.existsByEmail("jean.dupont@test.com")).thenReturn(false);
            when(serviceRepository.findByRefAndActifTrue("SVC-001")).thenReturn(Optional.of(service));

            var saved = Utilisateur.builder()
                    .id(2L).ref("RESP-001").email("jean.dupont@test.com")
                    .nom("Dupont").prenom("Jean").telephone("0600000002")
                    .role(RoleUtilisateur.RESPONSABLE).service(service).actif(true).build();

            when(utilisateurRepository.save(any())).thenReturn(saved);

            UtilisateurResponse result = utilisateurService.creerResponsable(buildRequest());

            assertThat(result.role()).isEqualTo(RoleUtilisateur.RESPONSABLE);
            assertThat(result.service().ref()).isEqualTo("SVC-001");
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le service n'existe pas")
        void shouldThrowWhenServiceNotFound() {
            when(utilisateurRepository.existsByRef("RESP-001")).thenReturn(false);
            when(utilisateurRepository.existsByEmail("jean.dupont@test.com")).thenReturn(false);
            when(serviceRepository.findByRefAndActifTrue("SVC-001")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> utilisateurService.creerResponsable(buildRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    //  scenario de test pour findByRef ────────────────────────────────────────────────

    @Nested
    @DisplayName("findByRef()")
    class FindByRef {

        @Test
        @DisplayName("Doit retourner l'utilisateur si la référence existe")
        void shouldReturnUtilisateurWhenFound() {
            var utilisateur = Utilisateur.builder()
                    .id(1L).ref("CLI-001").role(RoleUtilisateur.CLIENT).actif(true).build();

            when(utilisateurRepository.findByRef("CLI-001")).thenReturn(Optional.of(utilisateur));

            UtilisateurResponse result = utilisateurService.findByRef("CLI-001");

            assertThat(result.ref()).isEqualTo("CLI-001");
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si la référence est inconnue")
        void shouldThrowWhenNotFound() {
            when(utilisateurRepository.findByRef("INCONNU")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> utilisateurService.findByRef("INCONNU"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    //  scenario de test pour findByRole ───────────────────────────────────────────────

    @Nested
    @DisplayName("findByRole()")
    class FindByRole {

        @Test
        @DisplayName("Doit retourner uniquement les clients actifs")
        void shouldReturnOnlyActiveClients() {
            var client = Utilisateur.builder()
                    .id(1L).ref("CLI-001").role(RoleUtilisateur.CLIENT).actif(true).build();

            when(utilisateurRepository.findByRoleAndActifTrue(RoleUtilisateur.CLIENT))
                    .thenReturn(List.of(client));

            List<UtilisateurResponse> result = utilisateurService.findByRole(RoleUtilisateur.CLIENT);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).role()).isEqualTo(RoleUtilisateur.CLIENT);
        }
    }

}