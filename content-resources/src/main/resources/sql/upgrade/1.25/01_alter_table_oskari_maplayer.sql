-- add srs_name and version columns to oskari_maplayer table

ALTER TABLE oskari_maplayer
   ADD  srs_name character varying,
   ADD  version character varying(64);
