CREATE TABLE oskari_user_indicator
(
  id serial NOT NULL,
  user_id bigint,
  title character varying(1000),
  source character varying(1000),
  layer_id bigint,
  description character varying(1000),
  year bigint,
  data text,
  published BOOLEAN,
  category character varying(100)
)
WITH (
  OIDS=FALSE
);
