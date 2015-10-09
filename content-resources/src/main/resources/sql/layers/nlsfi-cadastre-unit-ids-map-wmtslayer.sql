-- NOTE! Terms of use are listed in here;
--  ;
-- THIS IS AN EXAMPLE FOR ADDING WMTS LAYER ;

INSERT INTO oskari_maplayer(type, name, groupId,
                            metadataId, url,
                            locale,
                            tile_matrix_set_id,
                            minScale, maxScale)
  VALUES('wmtslayer', 'kiinteistotunnukset', (select id from oskari_layergroup where locale like '%Maanmittauslaitos%' union select max(id) from oskari_layergroup limit 1),
         '472b3e52-5ba8-4967-8785-4fa13955b42e', 'http://karttamoottori.maanmittauslaitos.fi/kiinteisto/wmts',
         '{ fi:{name:"Kiinteistötunnukset",subtitle:"(WMTS)"},sv:{name:"Fastighetsbeteckningar",subtitle:"(WMTS)"},en:{name:"National Cadastral Refererences",subtitle:"(WMTS)"}}',
         'ETRS-TM35FIN', 3999, 1);

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((SELECT MAX(id) FROM oskari_maplayer),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Cadastral parcels%'));

-- setup permissions for guest user;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'http://karttamoottori.maanmittauslaitos.fi/kiinteisto/wmts+kiinteistotunnukset');

INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '10110');
