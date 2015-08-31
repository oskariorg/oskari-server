-- NOTE! Terms of use are listed in here;
--  ;
-- THIS IS AN EXAMPLE FOR ADDING WMTS LAYER ;
INSERT INTO oskari_maplayer(type, name, groupId,
                            metadataId, url,
                            locale,
                            tile_matrix_set_id)
  VALUES('wmtslayer', 'taustakartta', (select id from oskari_layergroup where locale like '%Maanmittauslaitos%' union select max(id) from oskari_layergroup limit 1),
         'c22da116-5095-4878-bb04-dd7db3a1a341', 'http://karttamoottori.maanmittauslaitos.fi/maasto/wmts',
         '{ fi:{name:"Taustakarttasarja",subtitle:"(WMTS)"},sv:{name:"Backgrundskartserie",subtitle:"(WMTS)"},en:{name:"Background map serie",subtitle:"(WMTS)"}}',
         'ETRS-TM35FIN');

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((SELECT MAX(id) FROM oskari_maplayer),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Background maps%'));

-- setup permissions for guest user;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'http://karttamoottori.maanmittauslaitos.fi/maasto/wmts+taustakartta');

INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '10110');
