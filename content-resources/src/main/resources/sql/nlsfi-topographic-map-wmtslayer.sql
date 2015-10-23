-- NOTE! Terms of use are listed in here;
--  ;
-- THIS IS AN EXAMPLE FOR ADDING WMTS LAYER ;

INSERT INTO oskari_maplayer(type, name, groupId,
                            metadataId, url,
                            locale,
                            tile_matrix_set_id)
  VALUES('wmtslayer', 'maastokartta', (select id from oskari_layergroup where locale like '%Maanmittauslaitos%' union select max(id) from oskari_layergroup limit 1),
         'a2cd4d67-ee20-47b7-b899-a4d72e72bb2d', 'http://karttamoottori.maanmittauslaitos.fi/maasto/wmts',
         '{ fi:{name:"Maastokartta",subtitle:"(WMTS)"},sv:{name:"Terrängkarta",subtitle:"(WMTS)"},en:{name:"Topographic Map",subtitle:"(WMTS)"}}',
         'ETRS-TM35FIN');

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((SELECT MAX(id) FROM oskari_maplayer),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Topographic maps%'));

-- setup permissions for guest user;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'http://karttamoottori.maanmittauslaitos.fi/maasto/wmts+maastokartta');

INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '10110');
