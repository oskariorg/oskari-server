BEGIN;

SELECT id AS maplayerid, externalid
INTO oskari_maplayer_externalid
FROM oskari_maplayer
WHERE externalid <> '';

ALTER TABLE oskari_maplayer_externalid ADD PRIMARY KEY (maplayerid);
ALTER TABLE oskari_maplayer_externalid ADD FOREIGN KEY (maplayerid) REFERENCES oskari_maplayer (id) ON DELETE CASCADE;
ALTER TABLE oskari_maplayer_externalid ALTER externalid SET NOT NULL;
ALTER TABLE oskari_maplayer_externalid ADD UNIQUE (externalid);

COMMIT;

ANALYZE oskari_maplayer_externalid;
