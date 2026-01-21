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
    owner_id integer
);

-- Création de la table Sound
CREATE TABLE peps.Sound (
    idsound SERIAL PRIMARY KEY,
    nom varchar(255) NOT NULL,
    type_son varchar(50) NOT NULL,
    extension varchar(10) NOT NULL,
    chemin varchar(500),
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
