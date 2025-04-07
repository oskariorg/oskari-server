UPDATE oskari_appsetup_bundles
 SET bundle_id = (select id from oskari_bundle where name='metadatasearch'),
     bundleinstance = 'metadatasearch'
 WHERE bundle_id = (select id from oskari_bundle where name='metadatacatalogue');

UPDATE oskari_appsetup_bundles
 SET bundle_id = (select id from oskari_bundle where name='featuredata'),
     bundleinstance = 'featuredata'
 WHERE bundle_id = (select id from oskari_bundle where name='featuredata2');
 