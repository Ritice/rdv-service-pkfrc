package com.rdv_service_pkfrc.intefration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rdv_service_pkfrc.dto.request.CreerClientRequest;
import com.rdv_service_pkfrc.dto.request.CreerRendezVousRequest;
import com.rdv_service_pkfrc.dto.request.CreerResponsableRequest;
import com.rdv_service_pkfrc.entity.PlageHoraire;
import com.rdv_service_pkfrc.entity.ServiceAdministratif;
import com.rdv_service_pkfrc.entity.enumeration.StatutRdv;
import com.rdv_service_pkfrc.repository.PlageHoraireRepository;
import com.rdv_service_pkfrc.repository.RendezVousRepository;
import com.rdv_service_pkfrc.repository.ServiceAdministratifRepository;
import com.rdv_service_pkfrc.repository.UtilisateurRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration couvrant le flux complet de création d'un rendez-vous :
 * création du service → responsable → client → rendez-vous.
 *
 * Utilise un profil "test" avec une base H2 en mémoire.
 * Chaque test est rollback automatiquement via @Transactional.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Intégration — Flux création Rendez-Vous")
class RendezVousIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private ServiceAdministratifRepository serviceRepository;
    @Autowired private PlageHoraireRepository plageRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private RendezVousRepository rdvRepository;

    private static final String BASE_URL_RDV          = "/api/v1/rendez-vous";
    private static final String BASE_URL_UTILISATEURS = "/api/v1/utilisateurs";

    private ServiceAdministratif service;
    private PlageHoraire plage;

    @BeforeEach
    void setUp() {
        // Insérer les données de référence directement en base
        service = serviceRepository.save(
                ServiceAdministratif.builder()
                        .ref("SVC-TEST").libelle("Service Test").actif(true).build()
        );

        plage = plageRepository.save(
                PlageHoraire.builder()
                        .heureDebut(LocalTime.of(9, 0))
                        .heureFin(LocalTime.of(10, 0))
                        .libelle("09h00 - 10h00")
                        .build()
        );
    }

    // Flux complet
    @Test
    @DisplayName("Flux complet : créer responsable + client + RDV → succès")
    void shouldCreateFullRdvFlowSuccessfully() throws Exception {

        // Créer le responsable
        var reqResponsable = new CreerResponsableRequest(
                "RESP-INT-001", "responsable@test.com",
                "0600000010", "Doe", "John", "SVC-TEST"
        );

        mockMvc.perform(post(BASE_URL_UTILISATEURS + "/responsables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqResponsable)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ref").value("RESP-INT-001"));

        // Créer le client
        var reqClient = new CreerClientRequest(
                "CLI-INT-001", "client@test.com",
                "0600000011", "Smith", "Jane"
        );

        mockMvc.perform(post(BASE_URL_UTILISATEURS + "/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqClient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ref").value("CLI-INT-001"));

        //Créer le rendez-vous
        var reqRdv = new CreerRendezVousRequest(
                "CLI-INT-001", "RDV-INT-001", "SVC-TEST", "RESP-INT-001",
                LocalDate.now().plusDays(5), LocalTime.of(9, 0),
                "Demande de document officiel", null
        );

        mockMvc.perform(post(BASE_URL_RDV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqRdv)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.refRdv").value("RDV-INT-001"))
                .andExpect(jsonPath("$.data.statut").value("PLANIFIE"))
                .andExpect(jsonPath("$.data.clients", hasSize(1)))
                .andExpect(jsonPath("$.data.nombreClients").value(1));

        // Vérifier en base
        var rdvEnBase = rdvRepository.findByRefRdv("RDV-INT-001");
        assertThat(rdvEnBase).isPresent();
        assertThat(rdvEnBase.get().getStatut()).isEqualTo(StatutRdv.PLANIFIE);
    }

    //Validation du délai

    @Test
    @DisplayName("Doit rejeter un RDV avec une date trop proche (< 2 jours)")
    void shouldRejectRdvWithDateTooClose() throws Exception {
        creerResponsableEnBase("RESP-INT-002", "resp2@test.com");
        creerClientEnBase("CLI-INT-002", "cli2@test.com");

        var reqRdv = new CreerRendezVousRequest(
                "CLI-INT-002", "RDV-INT-002", "SVC-TEST", "RESP-INT-002",
                LocalDate.now().plusDays(1), // délai insuffisant
                LocalTime.of(9, 0), "Motif", null
        );

        mockMvc.perform(post(BASE_URL_RDV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqRdv)))
                .andExpect(status().isUnprocessableEntity());
    }

    // Conflit responsable

    @Test
    @DisplayName("Doit rejeter un RDV en conflit sur la même plage/date/responsable")
    void shouldRejectRdvWithConflict() throws Exception {
        creerResponsableEnBase("RESP-INT-003", "resp3@test.com");
        creerClientEnBase("CLI-INT-003", "cli3@test.com");
        creerClientEnBase("CLI-INT-004", "cli4@test.com");

        var date = LocalDate.now().plusDays(5);

        // Premier RDV — succès
        var req1 = new CreerRendezVousRequest(
                "CLI-INT-003", "RDV-INT-003", "SVC-TEST", "RESP-INT-003",
                date, LocalTime.of(9, 0), "Motif 1", null
        );
        mockMvc.perform(post(BASE_URL_RDV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Deuxième RDV — même responsable, même plage, même date → conflit
        var req2 = new CreerRendezVousRequest(
                "CLI-INT-004", "RDV-INT-004", "SVC-TEST", "RESP-INT-003",
                date, LocalTime.of(9, 0), "Motif 2", null
        );
        mockMvc.perform(post(BASE_URL_RDV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict());
    }


    // Validation Bean

    @Test
    @DisplayName("Doit retourner 400 si les champs obligatoires sont manquants")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        // Corps vide → violation des contraintes @NotBlank / @NotNull
        mockMvc.perform(post(BASE_URL_RDV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    //Helpers privés

    private void creerResponsableEnBase(String ref, String email) throws Exception {
        var req = new CreerResponsableRequest(ref, email, "06000000XX", "Nom", "Prenom", "SVC-TEST");
        mockMvc.perform(post(BASE_URL_UTILISATEURS + "/responsables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    private void creerClientEnBase(String ref, String email) throws Exception {
        var req = new CreerClientRequest(ref, email, "06000000XX", "Nom", "Prenom");
        mockMvc.perform(post(BASE_URL_UTILISATEURS + "/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}