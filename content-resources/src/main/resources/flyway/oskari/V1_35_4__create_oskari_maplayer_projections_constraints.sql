ALTER TABLE IF EXISTS oskari_maplayer_projections DROP CONSTRAINT IF EXISTS oskari_maplayer_projections_pkey;
ALTER TABLE oskari_maplayer_projections ADD CONSTRAINT oskari_maplayer_projections_pkey PRIMARY KEY(id);

DROP index IF EXISTS oskari_maplayer_projections_name_index;
CREATE INDEX oskari_maplayer_projections_name_index on oskari_maplayer_projections USING btree(name);


DROP INDEX IF EXISTS oskari_maplayer_projections_maplayerid_idx;
CREATE INDEX oskari_maplayer_projections_maplayerid_idx
ON oskari_maplayer_projections
USING btree
(maplayerid);