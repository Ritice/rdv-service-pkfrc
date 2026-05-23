-- ============================================================
-- V2__seed_data.sql
-- Données initiales : services et plages horaires
-- ============================================================

-- Services administratifs
INSERT INTO service_administratif (ref, libelle) VALUES
     ('SVC-001', 'Archives'),
     ('SVC-002', 'DAF'),
     ('SVC-003', 'RH'),
     ('SVC-004', 'Comptabilité'),
     ('SVC-005', 'Affaires sociales');

-- Plages horaires (08h à 16h, durée 1h)
INSERT INTO plage_horaire (heure_debut, heure_fin, libelle) VALUES
    ('08:00', '09:00', '08h00 - 09h00'),
    ('09:00', '10:00', '09h00 - 10h00'),
    ('10:00', '11:00', '10h00 - 11h00'),
    ('11:00', '12:00', '11h00 - 12h00'),
    ('12:00', '13:00', '12h00 - 13h00'),
    ('13:00', '14:00', '13h00 - 14h00'),
    ('14:00', '15:00', '14h00 - 15h00'),
    ('15:00', '16:00', '15h00 - 16h00');