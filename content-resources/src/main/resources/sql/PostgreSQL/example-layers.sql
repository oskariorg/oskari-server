-- Map Layers;
INSERT INTO oskari_maplayer(type, name, groupId,
                            url,
                            locale)
  VALUES('wmslayer', 'osm_finland:osm-finland', (SELECT id FROM oskari_layergroup WHERE locale LIKE '%Demo layers%'),
         'http://avaa.tdata.fi/geoserver/osm_finland/wms',
         '{ fi:{name:"OpenStreetMap WMS ETRS-TM35FIN",subtitle:""},sv:{name:"OpenStreetMap WMS ETRS-TM35FIN",subtitle:""},en:{name:"OpenStreetMap WMS ETRS-TM35FIN",subtitle:""}}');

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((SELECT MAX(id) FROM oskari_maplayer),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Others%'));

-- add layer as resource for mapping permissions;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'wmslayer+http://avaa.tdata.fi/geoserver/osm_finland/wms+osm_finland:osm-finland');
