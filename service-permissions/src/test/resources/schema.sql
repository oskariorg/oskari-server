CREATE TABLE public.oskari_resource (
    id integer NOT NULL,
    resource_type character varying(100) NOT NULL,
    resource_mapping character varying(1000) NOT NULL
);

CREATE SEQUENCE public.oskari_resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.oskari_resource ALTER COLUMN id SET DEFAULT nextval('public.oskari_resource_id_seq');
ALTER TABLE public.oskari_resource ADD CONSTRAINT type_mapping UNIQUE (resource_type, resource_mapping);
CREATE INDEX oskari_resource_idx ON public.oskari_resource (resource_type, resource_mapping);

CREATE TABLE public.oskari_resource_permission (
    id integer NOT NULL,
    resource_id bigint NOT NULL,
    external_type character varying(100),
    permission character varying(100),
    external_id character varying(1000)
);

CREATE SEQUENCE public.oskari_resource_permission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.oskari_resource_permission ALTER COLUMN id SET DEFAULT nextval('public.oskari_resource_permission_id_seq');
ALTER TABLE public.oskari_resource_permission ADD CONSTRAINT oskari_resource_permission_pkey PRIMARY KEY (id);
CREATE INDEX oskari_resource_permission_resid_idx ON public.oskari_resource_permission (resource_id);
