# Klinikus — API de télé-expertise médicale

API REST sécurisée permettant à un **médecin généraliste** de demander l'avis d'un **médecin spécialiste** pour un patient. Le généraliste choisit une spécialité, puis un spécialiste, et envoie sa question avec un niveau de priorité. Le spécialiste consulte les demandes qui lui sont adressées et y répond avec son avis et ses recommandations.

L'API est un projet **séparé** de l'application du brief 1 (consultations, JSP) mais utilise la **même base de données** PostgreSQL.

---

## Sommaire

1. [Stack technique](#1-stack-technique)
2. [Architecture](#2-architecture)
3. [Modèle de données](#3-modèle-de-données)
4. [Installation et démarrage](#4-installation-et-démarrage)
5. [Authentification et sécurité](#5-authentification-et-sécurité)
6. [Comptes de test](#6-comptes-de-test)
7. [Référence des endpoints](#7-référence-des-endpoints)
8. [Gestion des erreurs](#8-gestion-des-erreurs)
9. [Tester avec Postman](#9-tester-avec-postman)
10. [Parcours de démonstration](#10-parcours-de-démonstration)
11. [Organisation du travail (Git)](#11-organisation-du-travail-git)
12. [Dépannage](#12-dépannage)

---

## 1. Stack technique

| Domaine | Technologie |
|---|---|
| Langage | Java 17 |
| Build | Maven (packaging WAR) |
| Serveur | Apache Tomcat 10.1 (Jakarta EE) |
| API REST | JAX-RS — Jersey 3.1 |
| JSON | Jackson 2.17 (+ module `jsr310` pour les dates) |
| Persistance | JPA / Hibernate 6.6 |
| Base de données | PostgreSQL |
| Hachage des mots de passe | bcrypt (`at.favre.lib:bcrypt`) |

---

## 2. Architecture

L'application suit une **architecture en couches** : chaque couche ne dépend que de celle située en dessous.

```
Requête HTTP
    │
    ▼
┌──────────────────────────────────────────────┐
│ filter      AuthFilter (Basic + rôles)       │  ← authentifie chaque requête
├──────────────────────────────────────────────┤
│ resource    Endpoints JAX-RS (@Path, @GET…)  │  ← HTTP uniquement, aucune logique métier
├──────────────────────────────────────────────┤
│ service     Règles métier et validations     │  ← 400 / 403 / 404 levés ici
├──────────────────────────────────────────────┤
│ repository  Accès aux données (JPA / JPQL)   │
├──────────────────────────────────────────────┤
│ model       Entités JPA et enums             │
└──────────────────────────────────────────────┘
```

### Structure des packages

```
ma.klinikus
├── RestApplication            @ApplicationPath("/api")
├── filter
│   ├── AuthFilter             ContainerRequestFilter : authentification + contrôle des rôles
│   ├── RolesAllowed           annotation personnalisée (rôles autorisés par endpoint)
│   ├── SecurityContextImpl    SecurityContext + UserPrincipal (id, email, rôle)
│   └── PasswordVerification   vérification bcrypt
├── resource
│   ├── AuthResource           /auth/login, /auth/me
│   ├── SpecialisteResource    /specialistes
│   ├── DemandeExpertiseResource  /demandes
│   ├── ObjectMapperProvider   configuration Jackson (dates ISO-8601)
│   ├── WebAppExceptionMapper  erreurs HTTP → JSON
│   └── GenericExceptionMapper erreurs inattendues → 500 JSON
├── service
│   ├── AuthService
│   ├── SpecialisteService
│   └── DemandeExpertiseService
├── repository
│   ├── JpaUtil
│   ├── UtilisateurRepository
│   ├── SpecialisteRepository
│   └── DemandeExpertiseRepository
└── model
    ├── Utilisateur, Specialiste, DemandeExpertise
    └── enums  Role, Specialite, Priorite, StatutDemande
```

---

## 3. Modèle de données

```
utilisateur ──1:1── specialiste ──1:N── demandes_expertise ──N:1── consultation
```

| Table | Colonnes principales |
|---|---|
| `utilisateur` | `id`, `nom`, `email` (unique), `mot_de_passe` (hash bcrypt), `role` |
| `specialiste` | `id`, `utilisateur_id` (unique), `specialite`, `tarif` (DH) |
| `demandes_expertise` | `id`, `consultation_id`, `specialiste_id`, `question`, `priorite`, `statut`, `avis`, `recommandations`, `date_creation` |
| `consultation` | Table existante du brief 1 |

### Énumérations

| Enum | Valeurs |
|---|---|
| `Role` | `INFIRMIER`, `GENERALISTE`, `SPECIALISTE` |
| `Specialite` | `CARDIOLOGIE`, `PNEUMOLOGIE`, `DERMATOLOGIE`, `NEUROLOGIE`, `ENDOCRINOLOGIE` |
| `Priorite` | `URGENTE`, `NORMALE`, `NON_URGENTE` |
| `StatutDemande` | `EN_ATTENTE`, `TERMINEE` |

Une demande est créée au statut `EN_ATTENTE` et passe à `TERMINEE` lorsque le spécialiste y répond.

---

## 4. Installation et démarrage

### Prérequis

- JDK 17
- Maven 3.9+
- PostgreSQL (base `klinikus_db`, déjà alimentée par l'application du brief 1)
- Apache Tomcat 10.1

### Étapes

**1. Configurer la base de données.** Renseigner l'URL, l'utilisateur et le mot de passe PostgreSQL dans `src/main/resources/META-INF/persistence.xml` (unité de persistance `klinikusPU`).

**2. Créer les données de test** (comptes et spécialistes) : exécuter le script SQL du projet (voir [section 6](#6-comptes-de-test)).

**3. Compiler le projet.**

```bash
mvn clean package
```

**4. Déployer sur Tomcat.** Copier `target/klinikus.war` dans le dossier `webapps/` de Tomcat, puis démarrer le serveur.

**5. Vérifier.**

```bash
curl -u medecin@klinikus.ma:<mot_de_passe> http://localhost:8080/klinikus/api/auth/me
```

L'URL de base de l'API est :

```
http://localhost:8080/klinikus/api
```

---

## 5. Authentification et sécurité

### Principe

L'API est **stateless** : aucune session HTTP n'est conservée. Chaque requête prouve l'identité de son auteur avec l'authentification **HTTP Basic**, c'est-à-dire l'en-tête :

```
Authorization: Basic base64(email:motdepasse)
```

### Fonctionnement

1. `AuthFilter` (un `ContainerRequestFilter` à la priorité `AUTHENTICATION`) intercepte chaque requête.
2. Il lit l'annotation `@RolesAllowed` de la méthode (à défaut, celle de la classe).
3. `AuthService` décode l'en-tête, recherche l'utilisateur par email et vérifie le mot de passe avec **bcrypt**.
4. L'identité (`id`, `email`, `rôle`) est placée dans le `SecurityContext`.
5. Le rôle est comparé à la liste autorisée par l'endpoint.

| Situation | Réponse |
|---|---|
| En-tête absent, mal formé, identifiants incorrects | **401** + en-tête `WWW-Authenticate` |
| Rôle non autorisé pour l'endpoint | **403** |
| Endpoint sans `@RolesAllowed` | **403** (*sécurisé par défaut*) |

### Règles de sécurité à retenir

- **L'identité n'est jamais lue dans la requête** (paramètre, corps, chemin) : elle vient uniquement du `SecurityContext`. Un spécialiste ne peut donc ni voir ni traiter les demandes d'un autre.
- **Sécurisé par défaut** : tout nouvel endpoint doit déclarer `@RolesAllowed`, sinon il est refusé.
- Les mots de passe sont stockés **hachés en bcrypt** ; ils ne sont jamais renvoyés par l'API (`@JsonIgnore`).
- ⚠️ Basic Auth transmet les identifiants en Base64 (non chiffré) : **utiliser HTTPS en production**.

### Matrice des droits

| Endpoint | INFIRMIER | GENERALISTE | SPECIALISTE |
|---|:-:|:-:|:-:|
| `POST /auth/login`, `GET /auth/me` | ✅ | ✅ | ✅ |
| `GET /specialistes`, `GET /specialistes/{id}` | ✅ | ✅ | ❌ |
| `POST /demandes` | ❌ | ✅ | ❌ |
| `GET /demandes?consultationId=` | ❌ | ✅ | ❌ |
| `GET /demandes?statut=` | ❌ | ❌ | ✅ |
| `PUT /demandes/{id}/reponse` | ❌ | ❌ | ✅ |

---

## 6. Comptes de test

| Rôle | Email | Mot de passe |
|---|---|---|
| Infirmier | `infirmier@klinikus.ma` | *à compléter* |
| Généraliste | `medecin@klinikus.ma` | *à compléter* |
| Spécialiste 1 | `specialiste@klinikus.ma` | *à compléter* |
| Spécialiste 2 | `specialiste2@klinikus.ma` | `Specialiste123!` |

Deux spécialistes sont nécessaires pour tester le contrôle de propriété (réponse à la demande d'un autre spécialiste → 403).

### Script SQL du second spécialiste

```sql
INSERT INTO utilisateur (nom, email, mot_de_passe, role) VALUES
('Dr Specialiste Deux', 'specialiste2@klinikus.ma',
 '$2a$10$tqnnbsSiEjcV3SnhR/S95eP5FgkuFnE1MQnAuxSC87u1S2ShXcySC', 'SPECIALISTE');

-- Rattacher le spécialiste n°1 à ce compte
UPDATE specialiste
SET utilisateur_id = (SELECT id FROM utilisateur WHERE email = 'specialiste2@klinikus.ma')
WHERE id = 1;
```

> Chaque ligne de `specialiste` doit pointer vers un `utilisateur` existant, sinon le spécialiste est introuvable via l'API.

### Générer un hash bcrypt

Les mots de passe se hachent en bcrypt (coût 10, préfixe `$2a$`). Exemple en Java :

```java
String hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(10, "MonMotDePasse".toCharArray());
```

---

## 7. Référence des endpoints

Tous les endpoints sont préfixés par `/api`. Les réponses sont au format JSON.

### 7.1 Authentification

#### `POST /auth/login` · `GET /auth/me`

Rôles : tous. Renvoie l'identité de l'utilisateur authentifié (utile pour vérifier ses identifiants).

```json
{ "id": 2, "nom": "Dr Test", "email": "medecin@klinikus.ma", "role": "GENERALISTE" }
```

### 7.2 Spécialistes

#### `GET /specialistes?specialite=CARDIOLOGIE`

Rôles : `GENERALISTE`, `INFIRMIER`. Liste les spécialistes d'une spécialité, triés par **tarif croissant**. Une spécialité inexistante renvoie **400**.

#### `GET /specialistes/{id}`

Rôles : `GENERALISTE`, `INFIRMIER`. Détail d'un spécialiste (404 s'il n'existe pas).

### 7.3 Demandes d'expertise

#### `POST /demandes` — Créer une demande

Rôle : `GENERALISTE`. Succès : **201**.

```json
{
  "consultationId": 1,
  "specialisteId": 3,
  "question": "ECG anormal, suspicion de trouble du rythme. Votre avis ?",
  "priorite": "URGENTE"
}
```

| Champ | Règle |
|---|---|
| `consultationId` | obligatoire, la consultation doit exister (sinon 404) |
| `specialisteId` | obligatoire, le spécialiste doit exister (sinon 404) |
| `question` | obligatoire, non vide |
| `priorite` | obligatoire : `URGENTE`, `NORMALE` ou `NON_URGENTE` |

La demande est créée avec le statut `EN_ATTENTE`.

#### `GET /demandes?statut=EN_ATTENTE` — Demandes reçues

Rôle : `SPECIALISTE`. Renvoie **uniquement les demandes adressées au spécialiste connecté** (identité lue dans le `SecurityContext`). Le paramètre `statut` est obligatoire et seule la valeur `EN_ATTENTE` est supportée.

#### `GET /demandes?consultationId=12` — Demandes d'une consultation

Rôle : `GENERALISTE`. Renvoie les demandes liées à une consultation, avec l'avis et les recommandations lorsqu'elles sont `TERMINEE`. `consultationId` est obligatoire (400 sinon) et la consultation doit exister (404 sinon).

> Les deux variantes de `GET /demandes` sont servies par **une seule méthode** de la ressource, qui choisit le comportement selon le rôle. Deux méthodes `@GET` sur le même chemin feraient échouer le démarrage de Jersey.

#### `PUT /demandes/{id}/reponse` — Répondre à une demande

Rôle : `SPECIALISTE` destinataire de la demande. Succès : **200**, la demande passe au statut `TERMINEE`.

```json
{
  "avis": "L'ECG montre une fibrillation auriculaire à réponse ventriculaire rapide.",
  "recommandations": "Holter ECG sur 24h, échocardiographie, bilan thyroïdien."
}
```

Ordre des contrôles :

1. Demande inexistante → **404**
2. Demande adressée à un autre spécialiste → **403**
3. `avis` ou `recommandations` vide ou absent → **400**
4. Demande déjà `TERMINEE` → refusée (une demande ne reçoit qu'une seule réponse)

---

## 8. Gestion des erreurs

Toutes les erreurs sont renvoyées dans le même format JSON :

```json
{ "status": 403, "erreur": "Cette demande est adressée à un autre spécialiste" }
```

| Code | Signification | Exemples |
|---|---|---|
| **200** | Succès | Lecture, réponse enregistrée |
| **201** | Ressource créée | Création d'une demande |
| **400** | Requête invalide | Question vide, priorité inconnue, `consultationId` manquant |
| **401** | Non authentifié | Identifiants absents ou incorrects |
| **403** | Accès refusé | Mauvais rôle, demande d'un autre spécialiste |
| **404** | Ressource introuvable | Consultation, spécialiste ou demande inexistant |
| **500** | Erreur interne | Détail journalisé côté serveur, message générique côté client |

---

## 9. Tester avec Postman

Deux fichiers sont fournis :

- `Klinikus_API.postman_collection.json` : 17 requêtes réparties en 4 dossiers (authentification, spécialistes, demandes, cas d'erreur), chacune avec un test automatique sur le code HTTP attendu.
- `Klinikus_API.postman_environment.json` : URL de base et comptes de test.

### Mise en place

1. Importer les deux fichiers dans Postman.
2. Sélectionner l'environnement **Klinikus - Local** et renseigner les mots de passe (`A_REMPLIR`).
3. Vérifier `baseUrl` : `http://localhost:8080/klinikus/api`.
4. Lancer la collection (**Run collection**) ou les dossiers un par un.

### Authentification dans Postman

L'authentification **Basic Auth** est définie au niveau de la collection (`{{email}}` / `{{password}}`). Les requêtes qui exigent un rôle précis la remplacent par les variables du compte concerné (`{{generaliste_email}}`, `{{specialiste_email}}`, etc.).

Le `POST /demandes` enregistre l'id créé dans la variable `{{demandeId}}`, réutilisée par le `PUT /demandes/{{demandeId}}/reponse` : il faut donc lancer le dossier **3 - Demandes** dans l'ordre.

---

## 10. Parcours de démonstration

Exemple complet avec `curl` (remplacer les mots de passe) :

```bash
BASE=http://localhost:8080/klinikus/api

# 1. Le généraliste liste les cardiologues
curl -u medecin@klinikus.ma:*** "$BASE/specialistes?specialite=CARDIOLOGIE"

# 2. Il crée une demande d'expertise
curl -u medecin@klinikus.ma:*** -X POST "$BASE/demandes" \
  -H "Content-Type: application/json" \
  -d '{"consultationId":1,"specialisteId":3,"question":"ECG anormal. Votre avis ?","priorite":"URGENTE"}'

# 3. Le spécialiste consulte ses demandes en attente
curl -u specialiste@klinikus.ma:*** "$BASE/demandes?statut=EN_ATTENTE"

# 4. Il répond à la demande
curl -u specialiste@klinikus.ma:*** -X PUT "$BASE/demandes/15/reponse" \
  -H "Content-Type: application/json" \
  -d '{"avis":"Fibrillation auriculaire.","recommandations":"Holter 24h, échocardiographie."}'

# 5. Le généraliste relit la réponse
curl -u medecin@klinikus.ma:*** "$BASE/demandes?consultationId=1"
```

### Vérifications de sécurité

| Requête | Résultat attendu |
|---|---|
| Sans identifiants | 401 |
| Mauvais mot de passe | 401 |
| Infirmier sur `POST /demandes` | 403 |
| Généraliste sur `PUT /demandes/{id}/reponse` | 403 |
| Spécialiste 2 répond à une demande du spécialiste 1 | 403 |
| `PUT /demandes/9999/reponse` | 404 |

---

## 11. Organisation du travail (Git)

Chaque membre du binôme est propriétaire de ses user stories de bout en bout (entité, repository, service, resource, requêtes Postman). L'authentification est réalisée à deux, en pair programming.

| User story | Contenu |
|---|---|
| US0 | Authentification Basic, `SecurityContext`, `@RolesAllowed` |
| US1 | Lister les spécialistes d'une spécialité |
| US2 | Créer une demande d'expertise |
| US3 | Consulter les demandes |
| US4 | Répondre à une demande |

- Une **branche par user story** (`feature/usX-...`), créée depuis `develop`.
- Chaque **pull request est relue par l'autre membre** avant fusion dans `develop`.
- `develop` est fusionnée dans `main` en fin de sprint, puis taguée `v1.0`.

---

## 12. Dépannage

| Symptôme | Cause probable | Solution |
|---|---|---|
| `403 — Endpoint non configuré : accès refusé` | Pas de `@RolesAllowed` sur l'endpoint, ou mauvais import | Utiliser `ma.klinikus.filter.RolesAllowed` (et non `jakarta.annotation.security.RolesAllowed`), puis redéployer |
| Démarrage : *ambiguous (sub-)resource method for HTTP method GET* | Deux méthodes `@GET` sur le même chemin | N'en garder qu'une et répartir selon le rôle |
| `500` à la création ou à la lecture d'une demande | Entité sérialisée avec une relation *lazy* non chargée | Charger la relation avec `JOIN FETCH` dans la requête |
| `404 — Spécialiste introuvable` alors qu'il existe en base | `specialiste.utilisateur_id` pointe vers un utilisateur inexistant | Corriger la donnée ou le rattachement |
| `401` avec des identifiants corrects | Hash bcrypt invalide ou mot de passe différent | Régénérer le hash et mettre à jour `utilisateur.mot_de_passe` |
| Modifications non prises en compte | WAR non recompilé ou non redéployé | `mvn clean package`, puis redéploiement et redémarrage de Tomcat |