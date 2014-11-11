-- move  overlapping values to oskari_maplayer table from portti_wfs_layer

UPDATE oskari_maplayer AS a
   SET 
       url=b.url FROM portti_wfs_layer As b   
       WHERE b.maplayer_id = a.id AND a.type = 'wfslayer';
       
UPDATE oskari_maplayer AS a
   SET 
       username=b.username FROM portti_wfs_layer As b
       WHERE b.maplayer_id = a.id AND a.type = 'wfslayer';

UPDATE oskari_maplayer AS a
   SET 
       password=b.password FROM portti_wfs_layer As b
       WHERE b.maplayer_id = a.id AND a.type = 'wfslayer';

UPDATE oskari_maplayer AS a
   SET 
       srs_name=b.srs_name FROM portti_wfs_layer As b
       WHERE b.maplayer_id = a.id AND a.type = 'wfslayer';

UPDATE oskari_maplayer AS a
   SET 
       version=b.wfs_version FROM portti_wfs_layer As b
       WHERE b.maplayer_id = a.id AND a.type = 'wfslayer';

