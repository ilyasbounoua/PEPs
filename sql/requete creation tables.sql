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
