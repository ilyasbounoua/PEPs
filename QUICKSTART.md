# Application PEPs - Guide de Démarrage Rapide

## Qu'est-ce que PEPs?

PEPs est un système de surveillance et de gestion web conçu pour suivre les interactions avec des modules intelligents. L'application fournit des statistiques en temps réel, une analyse de données historiques et des capacités de gestion de configuration.

## Installation Rapide (5 Minutes)

### 1. Base de Données (2 minutes)
```bash
# Démarrer PostgreSQL
pg_ctl start

# Créer la structure de la base de données
psql -U postgres -d postgres -f "sql/requete creation tables.sql"

# Charger les données de test
psql -U postgres -d postgres -f "sql/Creation données test.sql"
```

### 2. Backend (2 minutes)
```bash
# Compiler l'application
cd back\PEPs_back
mvn clean install

# Déployer sur le serveur d'applications
# Copier target/PEPs_back-0.1.war dans le dossier webapps de votre serveur

# Vérifier le déploiement
curl http://localhost:8080/PEPs_back/dashboard
```

### 3. Frontend (1 minute)
```bash
# Démarrer le serveur de développement
cd front\pepsfront
npm install
npm start

# Accéder à l'application
# Ouvrir http://localhost:4200
```

## Identifiants par Défaut

- **Nom d'utilisateur:** (aucun requis)
- **Mot de passe:** `admin`

## Fonctionnalités Principales

### Tableau de Bord
- Statistiques d'interactions en temps réel
- Nombre de modules actifs
- Horodatage de la dernière interaction
- Graphique d'activité horaire

### Interactions
- Historique complet des interactions
- Filtrage par date
- Capacité d'export CSV

### Modules
- Surveillance de l'état des modules
- Gestion de configuration
- Mises à jour en temps réel

### Bibliothèque de Sons
- Parcourir les sons disponibles
- Écouter les sons
- Ajouter de nouveaux sons avec upload de fichiers
- Supprimer des sons avec confirmation

## Ports par Défaut

- API Backend: `http://localhost:8080`
- Interface Frontend: `http://localhost:4200`
- Base de données: `localhost:5432`

## Exigences Minimales

- PostgreSQL 12+
- Java 8+
- Node.js 18+
- 2GB RAM
- Navigateur web moderne

## Problèmes Courants

**Impossible de se connecter au backend?**
- Assurez-vous que Tomcat/GlassFish est en cours d'exécution
- Vérifiez le déploiement au contexte `/PEPs_back`

**Aucune donnée affichée?**
- Exécutez le script SQL de données de test
- Vérifiez que les endpoints backend retournent des données

**Erreurs CORS?**
- Le frontend doit s'exécuter sur le port 4200
- Ou mettez à jour les paramètres CORS dans les contrôleurs backend

**Problèmes d'upload de son?**
- Formats supportés: mp3, wav, ogg, m4a
- Vérifiez que le dossier `sons` existe et est accessible
- Vérifiez les permissions d'écriture

## Structure du Projet

```
PEPs/
├── back/PEPs_back/         # Backend Java Spring
├── front/pepsfront/        # Frontend Angular  
├── sons/                   # Fichiers audio
├── sql/                    # Scripts de base de données
├── QUICKSTART_FR.md        # Ce fichier
└── SETUP_GUIDE_FR.md       # Documentation détaillée
```

## Prochaines Étapes

Après une installation réussie:
1. Connectez-vous avec les identifiants par défaut
2. Explorez le tableau de bord
3. Consultez les données d'exemple dans la page Interactions
4. Configurez un module de test
5. Ajoutez un nouveau son dans la bibliothèque
6. Consultez SETUP_GUIDE_FR.md pour la configuration avancée

## Support

Pour un dépannage détaillé et des options de configuration, consultez SETUP_GUIDE_FR.md dans le répertoire racine du projet.

---

**Version:** 1.0  
**Dernière mise à jour:** 2025-11-18
