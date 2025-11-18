# PEPs - Guide de Configuration Backend/Frontend

## Vue d'ensemble de l'Application

Cette application connecte un frontend Angular avec un backend Java Spring pour récupérer et mettre à jour des données réelles depuis une base de données PostgreSQL.

### Architecture de l'Application:

#### Backend (Java Spring)

Le backend fournit des endpoints API REST pour l'accès et la manipulation des données:

**Contrôleurs:**
- `DashBoardController.java` - Fournit les statistiques du tableau de bord
- `InteractionController.java` - Gère l'historique des interactions
- `ModuleController.java` - Gère les données et la configuration des modules
- `SoundController.java` - Gère la bibliothèque de sons avec upload/suppression de fichiers
- `DailyStatsController.java` - Fournit les statistiques horaires

**Objets de Transfert de Données:**
- `DashboardStats.java` - Structure des statistiques du tableau de bord
- `InteractionDTO.java` - Structure des données d'interaction
- `ModuleDTO.java`, `ModuleConfigDTO.java` - Structure des données de module
- `SoundDTO.java` - Structure des données de son
- `DailyDataDTO.java` - Structure des statistiques quotidiennes

**Configuration:**
- `web.xml` - Mappage de servlet configuré pour gérer les endpoints REST au chemin racine

#### Frontend (Angular)

Le frontend est une application monopage avec les fonctionnalités suivantes:

**Composants Principaux:**
- Tableau de bord avec statistiques en temps réel
- Historique des interactions avec filtrage par date
- Gestion des modules avec configuration
- Affichage de la bibliothèque de sons avec lecture et gestion

**Fonctionnalités:**
- Synchronisation des données en temps réel avec le backend
- Filtrage dynamique des dates pour les périodes actuelles
- Persistance de la configuration des modules avec validation
- Upload et suppression de fichiers son
- Lecture audio intégrée
- Fonctionnalité d'export CSV

---

## Configuration Système Requise

- PostgreSQL 12 ou supérieur
- Java JDK 8 ou supérieur
- Apache Maven 3.6 ou supérieur
- Node.js 18 ou supérieur avec npm
- Serveur d'Application (Apache Tomcat 9+ ou GlassFish 5+)
- Navigateur web moderne (Chrome, Firefox, Edge)

---

## Guide d'Installation

### Étape 1: Configuration de la Base de Données

1. Démarrez votre service PostgreSQL
   ```bash
   # Windows
   pg_ctl -D "C:\Program Files\PostgreSQL\XX\data" start
   
   # Linux/Mac
   sudo service postgresql start
   ```

2. Créez et peupler la base de données
   ```bash
   # Se connecter à PostgreSQL
   psql -U postgres -d postgres
   
   # Exécuter le script de création de tables
   \i sql/requete creation tables.sql
   
   # Exécuter le script de données de test
   \i sql/Creation données test.sql
   
   # Quitter psql
   \q
   ```

3. Vérifiez la configuration de la base de données
   ```bash
   psql -U postgres -d postgres -c "\dt"
   psql -U postgres -d postgres -c "SELECT COUNT(*) FROM interaction;"
   ```

4. Créez le dossier pour les fichiers son
   ```bash
   # À la racine du projet
   mkdir sons
   # Ou assurez-vous qu'il existe déjà
   ```

**Configuration de la Base de Données:**
Si vos identifiants PostgreSQL diffèrent des valeurs par défaut (utilisateur: postgres, mot de passe: postgres, port: 5432), mettez à jour les paramètres de connexion dans:
```
back/PEPs_back/src/main/resources/META-INF/persistence.xml
```

### Étape 2: Déploiement du Backend

1. Arrêtez toute instance de serveur d'application en cours d'exécution

2. Naviguez vers le répertoire backend
   ```bash
   cd back\PEPs_back
   ```

3. Compilez l'application
   ```bash
   mvn clean install
   ```

4. Déployez le fichier WAR
   - Localisez le fichier WAR: `target/PEPs_back-0.1.war`
   - Copiez-le dans le répertoire de déploiement de votre serveur d'applications:
     - Tomcat: `<TOMCAT_HOME>/webapps/`
     - GlassFish: Déployez via la console d'administration ou CLI
   - Assurez-vous que le chemin de contexte de déploiement est `/PEPs_back`

5. Démarrez le serveur d'applications

6. Vérifiez la fonctionnalité du backend
   
   Testez chaque endpoint dans votre navigateur ou avec curl:
   
   **Statistiques du Tableau de Bord:**
   ```
   http://localhost:8080/PEPs_back/dashboard
   ```
   Réponse attendue:
   ```json
   {"totalInteractions":4,"activeModules":2,"lastInteraction":"2025-01-18 10:30:45"}
   ```
   
   **Liste des Interactions:**
   ```
   http://localhost:8080/PEPs_back/interactions
   ```
   
   **Liste des Modules:**
   ```
   http://localhost:8080/PEPs_back/modules
   ```
   
   **Liste des Sons:**
   ```
   http://localhost:8080/PEPs_back/sounds
   ```
   
   **Statistiques Quotidiennes:**
   ```
   http://localhost:8080/PEPs_back/daily-stats
   ```

### Étape 3: Configuration du Frontend

1. Naviguez vers le répertoire frontend
   ```bash
   cd front\pepsfront
   ```

2. Installez les dépendances
   ```bash
   npm install
   ```

3. Démarrez le serveur de développement
   ```bash
   npm start
   ```

4. Accédez à l'application
   ```
   http://localhost:4200
   ```

### Étape 4: Utilisation de l'Application

1. **Connexion**
   - Mot de passe: `admin`
   - Le système utilise le hachage de mot de passe SHA-256

2. **Vue Tableau de Bord**
   - Affiche le nombre total d'interactions
   - Affiche le nombre de modules actifs
   - Affiche l'horodatage de l'interaction la plus récente
   - Affiche le graphique de distribution des interactions horaires

3. **Page Interactions**
   - Voir l'historique complet des interactions
   - Filtrer par période:
     - **Toutes**: Toutes les interactions
     - **Aujourd'hui**: Interactions d'aujourd'hui
     - **Hier**: Interactions d'hier
     - **Semaine**: 7 derniers jours
   - Exporter les données au format CSV

4. **Page Modules**
   - Voir tous les modules enregistrés
   - Accéder à la configuration du module:
     - Contrôle du volume (0-100%)
     - Mode de fonctionnement (Manuel/Automatique)
     - Basculer l'état actif
     - Activer/désactiver le son
   - Enregistrer les modifications de configuration dans la base de données
   - **Validation:** Le système valide:
     - Nom obligatoire
     - Adresse IP obligatoire et format valide
     - Volume entre 0 et 100
     - Mode valide (Manuel ou Automatique)

5. **Bibliothèque de Sons**
   - Parcourir les sons disponibles
   - Écouter les sons (bouton lecture/arrêt)
   - Ajouter de nouveaux sons:
     - Cliquer sur "Ajouter un son"
     - Entrer le nom du son
     - Sélectionner le type (Vocal, Ambiance, Naturel, Autre)
     - Choisir un fichier audio (mp3, wav, ogg, m4a)
     - Cliquer sur "Enregistrer"
   - Supprimer des sons:
     - Cliquer sur le bouton supprimer
     - Confirmer la suppression
     - Le fichier et l'entrée de base de données sont supprimés

6. **Actualiser les Données**
   - Utilisez le bouton d'actualisation pour recharger les données du backend
   - Toutes les pages récupèrent automatiquement les données actuelles au chargement

---

## Documentation de l'API

Le backend expose les endpoints API REST suivants:

### Statistiques du Tableau de Bord
**Endpoint:** `GET /dashboard`

**Description:** Retourne les statistiques globales du système

**Réponse:**
```json
{
  "totalInteractions": 4,
  "activeModules": 2,
  "lastInteraction": "2025-01-18 10:30:45"
}
```

### Liste des Interactions
**Endpoint:** `GET /interactions`

**Description:** Retourne tous les enregistrements d'interaction triés par date (plus récent en premier)

**Réponse:**
```json
[
  {
    "id": 1,
    "date": "2025-01-18T10:25:00",
    "module": "Module Perchoir 1",
    "type": "Bec"
  }
]
```

### Liste des Modules
**Endpoint:** `GET /modules`

**Description:** Retourne toutes les configurations de module

**Réponse:**
```json
[
  {
    "id": 1,
    "name": "Module Perchoir 1",
    "location": "",
    "status": "Actif",
    "ip": "192.168.1.10",
    "config": {
      "volume": 80,
      "mode": "Automatique",
      "actif": true,
      "son": false
    }
  }
]
```

### Mise à Jour de Module
**Endpoint:** `PUT /modules/{id}`

**Description:** Met à jour la configuration du module avec validation

**Paramètres:**
- `id` (paramètre de chemin): Identifiant du module

**Corps de la requête:**
```json
{
  "id": 1,
  "name": "Module Perchoir 1",
  "location": "Enclos Nord",
  "status": "Actif",
  "ip": "192.168.1.10",
  "config": {
    "volume": 85,
    "mode": "Manuel",
    "actif": true,
    "son": true
  }
}
```

**Validation:**
- Nom: obligatoire, ne peut pas être vide
- IP: obligatoire, doit correspondre au format IP valide (xxx.xxx.xxx.xxx)
- Volume: doit être entre 0 et 100
- Mode: doit être "Manuel" ou "Automatique"

**Réponse:** Objet module mis à jour

**Erreurs possibles:**
```json
{"error": "Le nom du module est obligatoire"}
{"error": "Format d'adresse IP invalide"}
{"error": "Le volume doit être entre 0 et 100"}
```

### Liste des Sons
**Endpoint:** `GET /sounds`

**Description:** Retourne tous les sons disponibles

**Réponse:**
```json
[
  {
    "id": 1,
    "name": "Chant Mali",
    "type": "Ambiance",
    "extension": "mp3",
    "fileName": "Chant_Mali.mp3"
  }
]
```

### Télécharger un Fichier Son
**Endpoint:** `GET /sounds/{id}/file`

**Description:** Télécharge le fichier audio d'un son spécifique

**Paramètres:**
- `id` (paramètre de chemin): Identifiant du son

**Réponse:** Flux de fichier audio avec en-têtes appropriés

### Upload de Son
**Endpoint:** `POST /sounds`

**Description:** Upload un nouveau son avec validation

**Type de contenu:** `multipart/form-data`

**Paramètres:**
- `name` (form-data): Nom du son (obligatoire)
- `type` (form-data): Type de son (obligatoire)
- `file` (form-data): Fichier audio (obligatoire)

**Validation:**
- Nom: obligatoire, ne peut pas être vide
- Type: obligatoire, ne peut pas être vide
- Fichier: obligatoire, formats acceptés: mp3, wav, ogg, m4a

**Réponse:** Objet son créé

**Erreurs possibles:**
```json
{"error": "Le nom est obligatoire"}
{"error": "Format de fichier non supporté. Utilisez mp3, wav, ogg ou m4a"}
{"error": "Erreur lors de l'enregistrement du fichier"}
```

### Suppression de Son
**Endpoint:** `DELETE /sounds/{id}`

**Description:** Supprime un son et son fichier associé

**Paramètres:**
- `id` (paramètre de chemin): Identifiant du son

**Réponse:**
```json
{"message": "Son supprimé avec succès"}
```

**Erreurs possibles:**
```json
{"error": "Son introuvable"}
{"error": "Erreur lors de la suppression du fichier"}
```

### Statistiques Quotidiennes
**Endpoint:** `GET /daily-stats`

**Description:** Retourne le nombre d'interactions groupées par intervalles de 2 heures pour le jour actuel

**Réponse:**
```json
[
  {"time": "8h", "count": 0},
  {"time": "10h", "count": 2},
  {"time": "12h", "count": 1},
  {"time": "14h", "count": 0},
  {"time": "16h", "count": 0},
  {"time": "18h", "count": 1}
]
```

---

## Guide de Dépannage

### Problèmes Backend

#### Échec de Connexion à la Base de Données

**Symptômes:**
- L'application ne démarre pas
- Erreurs de timeout de connexion
- Échecs d'authentification

**Solutions:**
1. Vérifiez que le service PostgreSQL est en cours d'exécution
   ```bash
   # Windows
   sc query postgresql-x64-XX
   
   # Linux/Mac
   sudo service postgresql status
   ```

2. Vérifiez les identifiants de base de données dans `persistence.xml`
   - Nom d'utilisateur: postgres
   - Mot de passe: postgres
   - Port: 5432
   - Base de données: postgres

3. Testez la connectivité de la base de données
   ```bash
   psql -U postgres -d postgres -c "SELECT 1"
   ```

#### Erreurs HTTP 404 sur les Endpoints API

**Symptômes:**
- Le navigateur affiche "404 Not Found"
- Les endpoints API ne sont pas accessibles

**Solutions:**
1. Vérifiez que le fichier WAR est correctement déployé
   - Vérifiez le répertoire webapps du serveur d'applications
   - Confirmez que le chemin de contexte de déploiement est `/PEPs_back`

2. Vérifiez le mappage de servlet dans `web.xml`
   - Doit être configuré avec `<url-pattern>/</url-pattern>`

3. Consultez les journaux du serveur d'applications

#### Problèmes d'Upload de Fichiers Son

**Symptômes:**
- Erreurs lors de l'upload
- Fichiers non sauvegardés

**Solutions:**
1. Vérifiez que le dossier `sons` existe
   ```bash
   ls sons/
   ```

2. Vérifiez les permissions d'écriture
   ```bash
   # Linux/Mac
   chmod 755 sons/
   ```

3. Vérifiez l'espace disque disponible

4. Vérifiez les limites de taille de fichier du serveur d'applications

### Problèmes Frontend

#### Erreurs CORS

**Symptômes:**
- La console du navigateur affiche des erreurs de politique CORS
- Requêtes réseau bloquées

**Solutions:**
1. Vérifiez la configuration CORS dans les contrôleurs
   - Doit inclure: `@CrossOrigin(origins = "http://localhost:4200")`

2. Si vous utilisez un port non standard, mettez à jour les origines CORS dans tous les contrôleurs

#### Problèmes de Lecture Audio

**Symptômes:**
- Les sons ne se jouent pas
- Erreurs dans la console

**Solutions:**
1. Vérifiez que le fichier son existe sur le serveur

2. Vérifiez les types MIME du serveur

3. Essayez différents navigateurs

4. Vérifiez la console du navigateur pour des erreurs détaillées

#### Problèmes de Validation de Configuration Module

**Symptômes:**
- Impossible de sauvegarder la configuration
- Messages d'erreur de validation

**Solutions:**
1. Vérifiez que tous les champs obligatoires sont remplis

2. Vérifiez le format de l'adresse IP (doit être xxx.xxx.xxx.xxx)

3. Vérifiez que le volume est entre 0 et 100

4. Vérifiez que le mode est "Manuel" ou "Automatique"

---

## Schéma de Base de Données

### Table Module
```sql
CREATE TABLE module (
  idmodule SERIAL PRIMARY KEY,
  nom VARCHAR(255) NOT NULL,
  ip_adress VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  volume INTEGER NOT NULL,
  current_mode VARCHAR(50) NOT NULL,
  actif BOOLEAN NOT NULL,
  last_seen TIMESTAMP NOT NULL
);
```

### Table Interaction
```sql
CREATE TABLE interaction (
  idinteraction SERIAL PRIMARY KEY,
  idsound INTEGER REFERENCES sound(idsound),
  idmodule INTEGER REFERENCES module(idmodule),
  typeInteraction VARCHAR(50) NOT NULL,
  time_lancement TIMESTAMP NOT NULL
);
```

### Table Sound
```sql
CREATE TABLE sound (
  idsound SERIAL PRIMARY KEY,
  nom VARCHAR(255) NOT NULL,
  type_son VARCHAR(50) NOT NULL,
  extension VARCHAR(10) NOT NULL
);
```

---

## Notes de Développement

### Structure du Projet

**Backend:**
```
back/PEPs_back/
├── src/main/java/peps/peps_back/
│   ├── controllers/       # Contrôleurs API REST
│   ├── items/             # Entités JPA
│   ├── repositories/      # Couche d'accès aux données
│   └── resources/         # Fichiers de configuration
├── src/main/webapp/
│   └── WEB-INF/
│       ├── web.xml        # Configuration servlet
│       ├── dispatcher-servlet.xml
│       └── applicationContext.xml
└── pom.xml                # Dépendances Maven
```

**Frontend:**
```
front/pepsfront/
├── src/app/
│   ├── app.ts             # Logique du composant principal
│   ├── app.html           # Template du composant principal
│   ├── app.css            # Styles du composant
│   ├── app.config.ts      # Configuration de l'application
│   └── app.routes.ts      # Configuration du routage
└── package.json           # Dépendances NPM
```

**Fichiers Son:**
```
sons/
├── Chant_Mali.mp3
├── parrot_cry_and_communication.mp3
└── water_flowing.wav
```

### Stack Technologique

**Backend:**
- Java 8+
- Spring Framework 5.3
- Spring Data JPA
- Pilote JDBC PostgreSQL
- Jackson pour la sérialisation JSON

**Frontend:**
- Angular 20+
- Angular Material 20+
- TypeScript 5+
- RxJS pour la programmation réactive

### Outils de Build
- Maven 3.6+ (Backend)
- npm (Frontend)

---

## Support et Maintenance

Pour les problèmes et questions:
1. Vérifiez les journaux du serveur d'applications pour des messages d'erreur détaillés
2. Consultez les journaux PostgreSQL pour les problèmes liés à la base de données
3. Utilisez les outils de développement du navigateur (F12) pour inspecter les requêtes réseau et les erreurs de console
4. Vérifiez que tous les services s'exécutent sur les ports attendus
5. Testez les endpoints API individuellement pour isoler les problèmes

Tâches de maintenance régulières:
- Surveiller la taille et les performances de la base de données
- Réviser et archiver les anciens enregistrements d'interaction
- Mettre à jour les dépendances pour les correctifs de sécurité
- Sauvegarder régulièrement la base de données et les fichiers son
- Surveiller les ressources du serveur d'applications
- Nettoyer les fichiers son inutilisés

---

**Version:** 1.0  
**Dernière mise à jour:** 2025-11-18

