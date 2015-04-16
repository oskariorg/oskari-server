-- add attributes column to oskari_maplayer table

ALTER TABLE oskari_maplayer
   ADD attributes text DEFAULT '{}';
