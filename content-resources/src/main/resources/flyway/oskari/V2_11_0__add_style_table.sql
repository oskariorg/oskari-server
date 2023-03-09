CREATE TABLE IF NOT EXISTS oskari_maplayer_style (
    id bigserial NOT NULL,
    layer character varying(64) NOT NULL,
    type character varying(20) NOT NULL,
    creator bigint DEFAULT (-1) NOT NULL,
    name character varying(256) NOT NULL,
    style text NOT NULL,
    created timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated timestamp with time zone,
    CONSTRAINT oskari_maplayer_style_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE oskari_maplayer_style IS 'Style configurations for vector maplayers';