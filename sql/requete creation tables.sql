CREATE TABLE public.Module (
    idmodule SERIAL NOT NULL PRIMARY KEY,
	nom character varying(255) NOT NULL,
	ip_adress character varying(50) NOT NULL,
	status character varying(50) NOT NULL,
	volume integer NOT NULL,
	current_mode character varying(50) NOT NULL,
	actif boolean NOT NULL,
	last_seen timestamp NOT NULL
);

CREATE TABLE public.Sound (
    idsound SERIAL NOT NULL PRIMARY KEY,
    nom character varying(255) NOT NULL,
    type_son character varying(50) NOT NULL,
    extension character varying(10) NOT NULL,
    chemin character varying(500)
);

CREATE TABLE public.Interaction (
	idinteraction Serial NOT NULL PRIMARY KEY,
	idsound integer ,
	idmodule integer ,
    typeInteraction character varying(50) NOT NULL,
	time_lancement timestamp NOT NULL DEFAULT NOW(),


	CONSTRAINT fk_idmodule
		FOREIGN KEY (idmodule)
		REFERENCES Module(idmodule)
		ON DELETE SET NULL,

	CONSTRAINT fk_idsound
		FOREIGN KEY (idsound)
		REFERENCES Sound(idsound)
		ON DELETE SET NULL
);

-- Table: public.users

-- DROP TABLE IF EXISTS public.users;

CREATE TABLE public.users
(
    id_user integer NOT NULL DEFAULT nextval('users_id_user_seq'::regclass),
    login character varying(100) COLLATE pg_catalog."default" NOT NULL,
    password_hash character varying(255) COLLATE pg_catalog."default" NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_pkey PRIMARY KEY (id_user),
    CONSTRAINT users_login_key UNIQUE (login)
)


