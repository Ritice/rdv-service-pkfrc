CREATE TABLE service_administratif (
   id          BIGSERIAL PRIMARY KEY,
   ref         VARCHAR(50)  NOT NULL UNIQUE,
   libelle     VARCHAR(100) NOT NULL UNIQUE,
   actif       BOOLEAN      NOT NULL DEFAULT TRUE,
   created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
   created_by VARCHAR(50) NOT NULL,
   updated_at TIMESTAMP   NOT NULL DEFAULT NOW(),
   updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE plage_horaire (
   id          BIGSERIAL PRIMARY KEY,
   heure_debut TIME         NOT NULL UNIQUE,
   heure_fin   TIME         NOT NULL,
   libelle     VARCHAR(20)  NOT NULL,
   created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
   created_by VARCHAR(50) NOT NULL,
   updated_at TIMESTAMP   NOT NULL DEFAULT NOW(),
   updated_by VARCHAR(50) NOT NULL,
   CONSTRAINT chk_plage_horaire CHECK (heure_fin > heure_debut)
);

CREATE TABLE utilisateur (
     id          BIGSERIAL PRIMARY KEY,
     ref         VARCHAR(50)  NOT NULL UNIQUE,
     email       VARCHAR(150) NOT NULL UNIQUE,
     telephone   VARCHAR(20)  NOT NULL,
     nom         VARCHAR(100) NOT NULL,
     prenom      VARCHAR(100) NOT NULL,
     role        VARCHAR(20)  NOT NULL CHECK (role IN ('CLIENT', 'RESPONSABLE')),
     ref_service BIGINT       REFERENCES service_administratif(id),
     actif       BOOLEAN      NOT NULL DEFAULT TRUE,
     version     BIGINT       NOT NULL DEFAULT 0,
     created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
     created_by VARCHAR(50) NOT NULL,
     updated_at TIMESTAMP   NOT NULL DEFAULT NOW(),
     updated_by VARCHAR(50) NOT NULL,
     CONSTRAINT chk_responsable_service
         CHECK (role != 'RESPONSABLE' OR ref_service IS NOT NULL)
    );

CREATE TABLE rendez_vous (
     id              BIGSERIAL    PRIMARY KEY,
     ref_rdv         VARCHAR(50)  NOT NULL UNIQUE,
     ref_service     BIGINT       NOT NULL REFERENCES service_administratif(id),
     ref_responsable BIGINT       NOT NULL REFERENCES utilisateur(id),
     ref_plage       BIGINT       NOT NULL REFERENCES plage_horaire(id),
     date_rdv        DATE         NOT NULL,
     motif_rdv       TEXT         NOT NULL,
     statut          VARCHAR(20)  NOT NULL DEFAULT 'PLANIFIE'
         CHECK (statut IN ('PLANIFIE', 'ANNULE', 'TERMINE')),
     version         BIGINT       NOT NULL DEFAULT 0,
     created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
     created_by VARCHAR(50) NOT NULL,
     updated_at TIMESTAMP   NOT NULL DEFAULT NOW(),
     updated_by VARCHAR(50) NOT NULL,
     CONSTRAINT uq_responsable_plage_date
         UNIQUE (ref_responsable, ref_plage, date_rdv)
);

CREATE TABLE rdv_client (
    rdv_id      BIGINT NOT NULL REFERENCES rendez_vous(id) ON DELETE CASCADE,
    client_id   BIGINT NOT NULL REFERENCES utilisateur(id),
    PRIMARY KEY (rdv_id, client_id)
);

CREATE INDEX idx_rdv_date         ON rendez_vous(date_rdv);
CREATE INDEX idx_rdv_service      ON rendez_vous(ref_service);
CREATE INDEX idx_rdv_responsable  ON rendez_vous(ref_responsable);
CREATE INDEX idx_utilisateur_role ON utilisateur(role);