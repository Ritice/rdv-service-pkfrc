# Service de Prise de RDV — PKFRC

API REST de gestion de rendez-vous administratifs.

## Stack Technique
Langage | Java 21 (Records, Switch Expressions, Text Blocks) |
Framework | Spring Boot 3.4.5 |
Base de données | PostgreSQL 15+ |
Migration BD | Flyway |
Tests | JUnit 5, Mockito, MockMvc |
Build | Maven 3.9+ |


## Prérequis
JDK 21** installé (`java -version`)
Maven 3.9+** (`mvn -version`)
PostgreSQL 15+** en cours d'exécution
(Optionnel) Docker pour lancer Postgres rapidement

---

## Lancement
### 1. Base de données PostgreSQL

**Avec Docker :**
```bash
docker run -d \
  --name rdv-postgres \
  -e POSTGRES_DB=rdv_pkfrc \
  -e POSTGRES_USER=ritice \
  -e POSTGRES_PASSWORD=ritice \
  -p 5433:5432 \
  postgres:15-alpine
```
**Ouvrir l'interface de pdAdmin a cette adrees**
 -localhost:5050

**Ou manuellement :**
```sql
CREATE DATABASE rdv_pkfrc;
```

### 2. Cloner et construire
git clone https://github.com/Ritice/rdv-service-pkfrc.git
cd rdv-service-pkfrc
mvn clean package -DskipTests


### 3. Lancer l'application

```bash
# Variables d'environnement (optionnel si postgres/postgres par défaut)
export DB_USERNAME=ritice
export DB_PASSWORD=ritice

mvn spring-boot:run
```

Ou directement :
```bash
java -jar target/rdv-service-1.0.0-SNAPSHOT.jar
```

L'application démarre sur **http://localhost:8080**.

Flyway applique automatiquement les migrations (schéma + données de référence).

---

## Exécuter les Tests

```bash
# Tous les tests (unitaires + intégration, H2 in-memory)
mvn test

# Tests unitaires seulement
mvn test -Dtest="*ServiceTest"

# Tests d'intégration seulement
mvn test -Dtest="*IntegrationTest"
```

> Les tests d'intégration utilisent un profil `test` avec H2 en mémoire — aucun PostgreSQL requis.

---

## Endpoints API

**lien de la Documentation OpenAPI JSON pour tester les different Endpoints**
## http://localhost:8080/swagger-ui/index.html#/

### Référentiel (données figées)
GET  `/api/v1/referentiel/services` | Liste des 5 services |
GET  `/api/v1/referentiel/plages` | Plages horaires 08h-16h |

### Utilisateurs
 POST  `/api/v1/utilisateurs/clients` | Créer un client |
 POST  `/api/v1/utilisateurs/responsables` | Créer un responsable |
 GET  `/api/v1/utilisateurs` | Lister tous les utilisateurs |
 GET  `/api/v1/utilisateurs?role=CLIENT` | Filtrer par rôle |
 GET  `/api/v1/utilisateurs/{ref}` | Consulter un utilisateur |
 DELETE  `/api/v1/utilisateurs/{ref}` | Désactiver un utilisateur |

### Rendez-vous
POST  `/api/v1/rendez-vous` | Créer un RDV |
GET  `/api/v1/rendez-vous` | Lister tous les RDV |
GET  `/api/v1/rendez-vous/{refRdv}` | Consulter un RDV |
GET  `/api/v1/rendez-vous/responsable/{ref}` | RDV d'un responsable |
GET  `/api/v1/rendez-vous/client/{ref}` | RDV d'un client |
POST  `/api/v1/rendez-vous/{refRdv}/clients` | Ajouter un client à un RDV |
PATCH  `/api/v1/rendez-vous/{refRdv}/annuler` | Annuler un RDV |


## Règles Métier

| 1 responsable par plage/jour | Contrainte UNIQUE BD + verrou pessimiste |
| Max 2 clients par RDV | Validation service + contrainte applicative |
| RDV ≥ 2 jours à l'avance | Validation `LocalDate` dans le service |
| Plages : 08h à 16h (1h chacune) | Données figées en BD via Flyway |
| 5 services disponibles | Données figées en BD via Flyway |

## Gestion de la Concurrence
La création simultanée de RDV est gérée à deux niveaux :

1. **Verrou pessimiste** (`PESSIMISTIC_WRITE`) sur la requête de vérification de conflit responsable — empêche deux transactions concurrentes de valider le même créneau simultanément.
2. **Verrou optimiste** (`@Version`) sur les entités `RendezVous` et `Utilisateur` — détecte les modifications concurrentes et retourne un HTTP 409 clair.
3. **Contrainte UNIQUE** en base de données (`responsable + plage + date`) comme filet de sécurité.


