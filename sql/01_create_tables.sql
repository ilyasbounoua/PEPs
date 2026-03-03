-- Création de la table Module
CREATE TABLE peps.Module (
    idmodule SERIAL PRIMARY KEY,
    nom varchar(255) NOT NULL,
    ip_adress varchar(50) NOT NULL,
    status varchar(50) NOT NULL,
    volume integer NOT NULL,
    current_mode varchar(50) NOT NULL,
    actif boolean NOT NULL,
    last_seen timestamp NOT NULL,
    version integer NOT NULL DEFAULT 0,
    owner_id integer
);

-- Création de la table Sound
CREATE TABLE peps.Sound (
    idsound SERIAL PRIMARY KEY,
    nom varchar(255) NOT NULL,
    type_son varchar(50) NOT NULL,
    extension varchar(10) NOT NULL,
    chemin varchar(500),
    version integer NOT NULL DEFAULT 0,
    owner_id integer
);

-- Création de la table Interaction
CREATE TABLE peps.Interaction (
    idinteraction SERIAL PRIMARY KEY,
    idsound integer,
    idmodule integer,
    typeInteraction varchar(50) NOT NULL,
    time_lancement timestamp NOT NULL DEFAULT NOW(),
    owner_id integer,

    CONSTRAINT fk_idmodule
        FOREIGN KEY (idmodule)
        REFERENCES peps.Module(idmodule)
        ON DELETE SET NULL,

    CONSTRAINT fk_idsound
        FOREIGN KEY (idsound)
        REFERENCES peps.Sound(idsound)
        ON DELETE SET NULL
);

-- Création de la table Users
CREATE TABLE peps.users (
    id_user SERIAL PRIMARY KEY,
    login varchar(100) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    role varchar(50)
);

-- TODO: [Ticket Pending] This table was missing in main. Created manually to unblock Archive feature.
-- Please verify column types matches AuditLog.java exactly.
CREATE TABLE peps.audit_logs (
    id SERIAL PRIMARY KEY,
    action varchar(20) NOT NULL,
    entity_type varchar(50) NOT NULL,
    entity_id integer,
    entity_name varchar(255),
    entity_role varchar(50),
    user_login varchar(100) NOT NULL,
    "timestamp" timestamp NOT NULL DEFAULT NOW(),
    old_value TEXT,
    new_value TEXT,
    details TEXT
);

