
-- Layer Group;
INSERT INTO oskari_layergroup (locale) values ('{ fi:{name:"Demo karttatasoja"},sv:{name:"Demo kartor"},en:{name:"Demo layers"}}');

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
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'http://avaa.tdata.fi/geoserver/osm_finland/wms+osm_finland:osm-finland');

-- permissions;
-- adding permissions to roles with id 10110, 2, and 3;

-- give view_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '10110');

-- give view_layer permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '2');

-- give publish permission for the resource to ROLE 3 (admin);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'PUBLISH', '3');

-- give view_published_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_PUBLISHED', '10110');

-- give view_published_layer permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_PUBLISHED', '2');



-- Add tutorial layers here;
