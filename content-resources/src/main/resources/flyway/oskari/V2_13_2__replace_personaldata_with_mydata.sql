UPDATE oskari_appsetup_bundles
 SET bundle_id = (select id from oskari_bundle where name='mydata'),
     bundleinstance = 'mydata'
 WHERE bundle_id = (select id from oskari_bundle where name='personaldata');