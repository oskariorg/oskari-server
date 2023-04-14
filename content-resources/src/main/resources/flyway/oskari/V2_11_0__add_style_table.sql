CREATE TABLE IF NOT EXISTS oskari_maplayer_style (
    id bigserial NOT NULL,
    layer_id integer,
    type character varying(20) NOT NULL,
    creator bigint,
    name character varying(256) NOT NULL,
    style text NOT NULL,
    created timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated timestamp with time zone,
    CONSTRAINT oskari_maplayer_style_pkey PRIMARY KEY (id),
    CONSTRAINT oskari_maplayer_style_creator_fkey FOREIGN KEY (creator)
          REFERENCES oskari_users(id) ON DELETE CASCADE,
    CONSTRAINT oskari_maplayer_style_layer_fkey FOREIGN KEY (layer_id)
          REFERENCES oskari_maplayer(id) ON DELETE CASCADE
);

COMMENT ON TABLE oskari_maplayer_style IS 'Style configurations for vector maplayers';