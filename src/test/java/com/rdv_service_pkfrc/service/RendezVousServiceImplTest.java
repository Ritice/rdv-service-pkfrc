package com.rdv_service_pkfrc.service;


import com.rdv_service_pkfrc.dto.request.AjouterClientRdvRequest;
import com.rdv_service_pkfrc.dto.request.CreerRendezVousRequest;
import com.rdv_service_pkfrc.dto.response.RendezVousResponse;
import com.rdv_service_pkfrc.entity.PlageHoraire;
import com.rdv_service_pkfrc.entity.RendezVous;
import com.rdv_service_pkfrc.entity.ServiceAdministratif;
import com.rdv_service_pkfrc.entity.Utilisateur;
import com.rdv_service_pkfrc.entity.enumeration.RoleUtilisateur;
import com.rdv_service_pkfrc.entity.enumeration.StatutRdv;
import com.rdv_service_pkfrc.exception.*;
import com.rdv_service_pkfrc.repository.PlageHoraireRepository;
import com.rdv_service_pkfrc.repository.RendezVousRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RendezVousServiceImpl — Tests unitaires")
public class RendezVousServiceImplTest {


    @Mock private RendezVousRepository rdvRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private ServiceAdministratifRepository serviceRepository;
    @Mock private PlageHoraireRepository plageRepository;

    @InjectMocks
    private RendezVousServiceImpl rdvService;

    private ServiceAdministratif service;
    private Utilisateur responsable;
    private Utilisateur client;
    private PlageHoraire plage;
    private LocalDate dateFuture;


    @BeforeEach
    void setUp() {
        service = ServiceAdministratif.builder()
                .id(1L).ref("SVC-001").libelle("Archives").actif(true).build();

        responsable = Utilisateur.builder()
                .id(2L).ref("RESP-001").nom("Dupont").prenom("Jean")
                .email("jean.dupont@test.com").telephone("0600000001")
                .role(RoleUtilisateur.RESPONSABLE).actif(true).service(service).build();

        client = Utilisateur.builder()
                .id(3L).ref("CLI-001").nom("Martin").prenom("Alice")
                .email("alice.martin@test.com").telephone("0600000002")
                .role(RoleUtilisateur.CLIENT).actif(true).build();

        plage = PlageHoraire.builder()
                .id(1L).heureDebut(LocalTime.of(9, 0))
                .heureFin(LocalTime.of(10, 0)).libelle("09h00 - 10h00").build();

        dateFuture = LocalDate.now().plusDays(5);
    }


    // ── senario de test pour la creation de RendezVous

    @Nested
    @DisplayName("creerRendezVous()")
    class CreerRendezVous {

        private CreerRendezVousRequest buildRequest() {
            return new CreerRendezVousRequest(
                    "CLI-001", "RDV-001", "SVC-001", "RESP-001",
                    dateFuture, LocalTime.of(9, 0), "Demande de document", null
            );
        }

        @Test
        @DisplayName("Doit créer un RDV valide avec succès")
        void shouldCreateRdvSuccessfully() {
            var req = buildRequest();

            when(rdvRepository.existsByRefRdv("RDV-001")).thenReturn(false);
            when(serviceRepository.findByRefAndActifTrue("SVC-001")).thenReturn(Optional.of(service));
            when(utilisateurRepository.findByRefAndRole("RESP-001", RoleUtilisateur.RESPONSABLE))
                    .thenReturn(Optional.of(responsable));
            when(plageRepository.findByHeureDebut(LocalTime.of(9, 0))).thenReturn(Optional.of(plage));
            when(rdvRepository.findConflitResponsable(anyLong(), anyLong(), any())).thenReturn(Optional.empty());
            when(utilisateurRepository.findByRefAndActifTrue("CLI-001")).thenReturn(Optional.of(client));

            var rdvSaved = RendezVous.builder()
                    .id(1L).refRdv("RDV-001").service(service).responsable(responsable)
                    .plageHoraire(plage).dateRdv(dateFuture).motifRdv("Demande de document")
                    .statut(StatutRdv.PLANIFIE).build();
            rdvSaved.ajouterClient(client);

            when(rdvRepository.save(any())).thenReturn(rdvSaved);

            RendezVousResponse result = rdvService.creerRendezVous(req);

            assertThat(result).isNotNull();
            assertThat(result.refRdv()).isEqualTo("RDV-001");
            assertThat(result.statut()).isEqualTo(StatutRdv.PLANIFIE);
            verify(rdvRepository).save(any(RendezVous.class));
        }

        @Test
        @DisplayName("Doit lever DuplicateReferenceException si la référence RDV existe déjà")
        void shouldThrowWhenRefRdvAlreadyExists() {
            when(rdvRepository.existsByRefRdv("RDV-001")).thenReturn(true);

            assertThatThrownBy(() -> rdvService.creerRendezVous(buildRequest()))
                    .isInstanceOf(DuplicateReferenceException.class)
                    .hasMessageContaining("RDV-001");

            verify(rdvRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever BusinessException si la date est trop proche (< 2 jours)")
        void shouldThrowWhenDateTooClose() {
            var req = new CreerRendezVousRequest(
                    "CLI-001", "RDV-001", "SVC-001", "RESP-001",
                    LocalDate.now().plusDays(1), // délai insuffisant
                    LocalTime.of(9, 0), "Motif", null
            );

            when(rdvRepository.existsByRefRdv("RDV-001")).thenReturn(false);

            assertThatThrownBy(() -> rdvService.creerRendezVous(req))
                    .isInstanceOf(DelaiRdvException.class);

            verify(rdvRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le service n'existe pas")
        void shouldThrowWhenServiceNotFound() {
            when(rdvRepository.existsByRefRdv("RDV-001")).thenReturn(false);
            when(serviceRepository.findByRefAndActifTrue("SVC-001")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rdvService.creerRendezVous(buildRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Doit lever ConflitRdvException si le responsable a déjà un RDV sur cette plage")
        void shouldThrowWhenResponsableHasConflict() {
            var rdvExistant = RendezVous.builder().id(99L).build();

            when(rdvRepository.existsByRefRdv("RDV-001")).thenReturn(false);
            when(serviceRepository.findByRefAndActifTrue("SVC-001")).thenReturn(Optional.of(service));
            when(utilisateurRepository.findByRefAndRole("RESP-001", RoleUtilisateur.RESPONSABLE))
                    .thenReturn(Optional.of(responsable));
            when(plageRepository.findByHeureDebut(any())).thenReturn(Optional.of(plage));
            when(rdvRepository.findConflitResponsable(anyLong(), anyLong(), any()))
                    .thenReturn(Optional.of(rdvExistant));

            assertThatThrownBy(() -> rdvService.creerRendezVous(buildRequest()))
                    .isInstanceOf(ConflitRdvException.class);

            verify(rdvRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever CapaciteRdvException si plus de 2 clients")
        void shouldThrowWhenTooManyClients() {
            var client2 = Utilisateur.builder().id(4L).ref("CLI-002")
                    .role(RoleUtilisateur.CLIENT).actif(true).build();
            var client3 = Utilisateur.builder().id(5L).ref("CLI-003")
                    .role(RoleUtilisateur.CLIENT).actif(true).build();

            var req = new CreerRendezVousRequest(
                    "CLI-001", "RDV-001", "SVC-001", "RESP-001",
                    dateFuture, LocalTime.of(9, 0), "Motif",
                    List.of("CLI-002", "CLI-003") // 3 clients au total
            );

            when(rdvRepository.existsByRefRdv("RDV-001")).thenReturn(false);
            when(serviceRepository.findByRefAndActifTrue("SVC-001")).thenReturn(Optional.of(service));
            when(utilisateurRepository.findByRefAndRole("RESP-001", RoleUtilisateur.RESPONSABLE))
                    .thenReturn(Optional.of(responsable));
            when(plageRepository.findByHeureDebut(any())).thenReturn(Optional.of(plage));
            when(rdvRepository.findConflitResponsable(anyLong(), anyLong(), any())).thenReturn(Optional.empty());
            when(utilisateurRepository.findByRefAndActifTrue("CLI-001")).thenReturn(Optional.of(client));
            when(utilisateurRepository.findByRefAndActifTrue("CLI-002")).thenReturn(Optional.of(client2));
            when(utilisateurRepository.findByRefAndActifTrue("CLI-003")).thenReturn(Optional.of(client3));

            assertThatThrownBy(() -> rdvService.creerRendezVous(req))
                    .isInstanceOf(CapaciteRdvException.class);
        }
    }

    // ── senario de test pour ajouterClientAuRdv

    @Nested
    @DisplayName("ajouterClientAuRdv()")
    class AjouterClientAuRdv {

        @Test
        @DisplayName("Doit ajouter un client à un RDV planifié")
        void shouldAddClientSuccessfully() {
            var rdv = RendezVous.builder()
                    .id(1L).refRdv("RDV-001").statut(StatutRdv.PLANIFIE)
                    .service(service).responsable(responsable).plageHoraire(plage)
                    .dateRdv(dateFuture).motifRdv("Motif").build();
            rdv.ajouterClient(client);

            var client2 = Utilisateur.builder().id(4L).ref("CLI-002")
                    .role(RoleUtilisateur.CLIENT).actif(true).build();

            when(rdvRepository.findByRefRdv("RDV-001")).thenReturn(Optional.of(rdv));
            when(utilisateurRepository.findByRefAndActifTrue("CLI-002")).thenReturn(Optional.of(client2));
            when(rdvRepository.save(any())).thenReturn(rdv);

            var req = new AjouterClientRdvRequest("CLI-002");
            RendezVousResponse result = rdvService.ajouterClientAuRdv("RDV-001", req);

            assertThat(result).isNotNull();
            verify(rdvRepository).save(rdv);
        }

        @Test
        @DisplayName("Doit lever BusinessException si le RDV est annulé")
        void shouldThrowWhenRdvAnnule() {
            var rdv = RendezVous.builder()
                    .id(1L).refRdv("RDV-001").statut(StatutRdv.ANNULE).build();

            when(rdvRepository.findByRefRdv("RDV-001")).thenReturn(Optional.of(rdv));

            assertThatThrownBy(() -> rdvService.ajouterClientAuRdv("RDV-001", new AjouterClientRdvRequest("CLI-002")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("annule");
        }

        @Test
        @DisplayName("Doit lever CapaciteRdvException si le RDV est complet")
        void shouldThrowWhenRdvFull() {
            var client2 = Utilisateur.builder().id(4L).ref("CLI-002")
                    .role(RoleUtilisateur.CLIENT).actif(true).build();

            var rdv = RendezVous.builder()
                    .id(1L).refRdv("RDV-001").statut(StatutRdv.PLANIFIE).build();
            rdv.ajouterClient(client);
            rdv.ajouterClient(client2);

            when(rdvRepository.findByRefRdv("RDV-001")).thenReturn(Optional.of(rdv));

            assertThatThrownBy(() -> rdvService.ajouterClientAuRdv("RDV-001", new AjouterClientRdvRequest("CLI-003")))
                    .isInstanceOf(CapaciteRdvException.class);
        }

        @Test
        @DisplayName("Doit lever BusinessException si le client est déjà présent")
        void shouldThrowWhenClientAlreadyPresent() {
            var rdv = RendezVous.builder()
                    .id(1L).refRdv("RDV-001").statut(StatutRdv.PLANIFIE).build();
            rdv.ajouterClient(client);

            when(rdvRepository.findByRefRdv("RDV-001")).thenReturn(Optional.of(rdv));

            assertThatThrownBy(() -> rdvService.ajouterClientAuRdv("RDV-001", new AjouterClientRdvRequest("CLI-001")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("déjà associé");
        }
    }

    // ── findByRef ────────────────────────────────────────────────

    @Nested
    @DisplayName("findByRef()")
    class FindByRef {

        @Test
        @DisplayName("Doit retourner le RDV si la référence existe")
        void shouldReturnRdvWhenFound() {
            var rdv = RendezVous.builder()
                    .id(1L).refRdv("RDV-001").statut(StatutRdv.PLANIFIE)
                    .service(service).responsable(responsable).plageHoraire(plage)
                    .dateRdv(dateFuture).motifRdv("Motif").build();

            when(rdvRepository.findByRefRdv("RDV-001")).thenReturn(Optional.of(rdv));

            RendezVousResponse result = rdvService.findByRef("RDV-001");

            assertThat(result.refRdv()).isEqualTo("RDV-001");
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si la référence est inconnue")
        void shouldThrowWhenNotFound() {
            when(rdvRepository.findByRefRdv("INCONNU")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rdvService.findByRef("INCONNU"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
