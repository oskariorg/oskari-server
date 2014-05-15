-- NOTE!;
-- THE FILE IS TOKENIZED WITH SEMICOLON CHARACTER!;
-- EACH COMMENT _NEED_ TO END WITH A SEMICOLON OR OTHERWISE THE NEXT ACTUAL SQL IS NOT RUN!;
-- ----------------------------------------------------------------------------------------;

DROP TABLE IF EXISTS portti_layer_keywords;
DROP TABLE IF EXISTS portti_keyword_association;
DROP TABLE IF EXISTS portti_keywords;

CREATE TABLE portti_keywords
(
  id serial NOT NULL,
  keyword character varying(2000),
  uri character varying(2000),
  lang character varying(10),
  editable boolean,
  CONSTRAINT portti_keywords_pkey PRIMARY KEY (id)
);

CREATE TABLE portti_layer_keywords
(
  keyid bigint NOT NULL,
  layerid bigint NOT NULL,
  CONSTRAINT oskari_layer_keywords_layerid_fkey FOREIGN KEY (layerid)
  REFERENCES oskari_maplayer (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT portti_layer_keywords_keyid_fkey FOREIGN KEY (keyid)
  REFERENCES portti_keywords (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE portti_keyword_association
(
  keyid1 bigint NOT NULL,
  keyid2 bigint NOT NULL,
  type character varying(10),
  CONSTRAINT portti_keyword_association_keyid1_fkey FOREIGN KEY (keyid1)
  REFERENCES portti_keywords (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT portti_keyword_association_keyid2_fkey FOREIGN KEY (keyid2)
  REFERENCES portti_keywords (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT unique_all_columns UNIQUE (keyid1, keyid2, type)
);