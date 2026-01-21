-- Création de la table Module
CREATE TABLE public.Module (
    idmodule SERIAL NOT NULL PRIMARY KEY,
    nom character varying(255) NOT NULL,
    ip_adress character varying(50) NOT NULL,
    status character varying(50) NOT NULL,
    volume integer NOT NULL,
    current_mode character varying(50) NOT NULL,
    actif boolean NOT NULL,
    last_seen timestamp NOT NULL,
    owner_id integer -- Ajouté pour la supervision (n'oubliez pas ce champ s'il n'est pas dans votre script original)
);

-- Création de la table Sound
CREATE TABLE public.Sound (
    idsound SERIAL NOT NULL PRIMARY KEY,
    nom character varying(255) NOT NULL,
    type_son character varying(50) NOT NULL,
    extension character varying(10) NOT NULL,
    chemin character varying(500),
    owner_id integer -- Ajouté pour la supervision
);

-- Création de la table Interaction
CREATE TABLE public.Interaction (
    idinteraction Serial NOT NULL PRIMARY KEY,
    idsound integer,
    idmodule integer,
    typeInteraction character varying(50) NOT NULL,
    time_lancement timestamp NOT NULL DEFAULT NOW(),
    owner_id integer, -- Ajouté pour la supervision

    CONSTRAINT fk_idmodule
        FOREIGN KEY (idmodule)
        REFERENCES Module(idmodule)
        ON DELETE SET NULL,

    CONSTRAINT fk_idsound
        FOREIGN KEY (idsound)
        REFERENCES Sound(idsound)
        ON DELETE SET NULL
);

-- Création de la table Users
-- (J'ai simplifié la définition pour utiliser SERIAL directement et éviter les erreurs de séquence manquante)
CREATE TABLE public.users
(
    id_user SERIAL NOT NULL PRIMARY KEY,
    login character varying(100) NOT NULL UNIQUE,
    password_hash character varying(255) NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    role character varying(50) -- Important pour les rôles (admin, dauphin, aras)
);