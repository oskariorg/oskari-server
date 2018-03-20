CREATE TABLE oskari_user_indicator_data
(
  id serial NOT NULL,
  indicator_id integer NOT NULL,
  regionset_id integer NOT NULL,
  year integer NOT NULL,
  data text NOT NULL,
  CONSTRAINT oskari_user_indicator_data_pkey PRIMARY KEY (id),
  CONSTRAINT oskari_user_indicator_id_fkey FOREIGN KEY (indicator_id)
      REFERENCES oskari_user_indicator (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT oskari_user_indicator_regionset_fkey FOREIGN KEY (regionset_id)
      REFERENCES oskari_maplayer (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT oskari_user_indicator_data_indicator_year UNIQUE (indicator_id, regionset_id, year)
);