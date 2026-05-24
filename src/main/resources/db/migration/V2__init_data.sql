
-- Données initiales : services et plages horaires

INSERT INTO service_administratif (ref, libelle, created_at, created_by, updated_at, updated_by) VALUES
('SVC-001', 'Archives',        NOW(), 'system', NOW(), 'system'),
('SVC-002', 'DAF',             NOW(), 'system', NOW(), 'system'),
('SVC-003', 'RH',              NOW(), 'system', NOW(), 'system'),
('SVC-004', 'Comptabilité',    NOW(), 'system', NOW(), 'system'),
('SVC-005', 'Affaires sociales', NOW(), 'system', NOW(), 'system');

-- Plages horaires (08h à 16h, durée 1h)
INSERT INTO plage_horaire (heure_debut, heure_fin, libelle, created_at, created_by, updated_at, updated_by) VALUES
('08:00', '09:00', '08h00 - 09h00', NOW(), 'system', NOW(), 'system'),
('09:00', '10:00', '09h00 - 10h00', NOW(), 'system', NOW(), 'system'),
('10:00', '11:00', '10h00 - 11h00', NOW(), 'system', NOW(), 'system'),
('11:00', '12:00', '11h00 - 12h00', NOW(), 'system', NOW(), 'system'),
('12:00', '13:00', '12h00 - 13h00', NOW(), 'system', NOW(), 'system'),
('13:00', '14:00', '13h00 - 14h00', NOW(), 'system', NOW(), 'system'),
('14:00', '15:00', '14h00 - 15h00', NOW(), 'system', NOW(), 'system'),
('15:00', '16:00', '15h00 - 16h00', NOW(), 'system', NOW(), 'system');