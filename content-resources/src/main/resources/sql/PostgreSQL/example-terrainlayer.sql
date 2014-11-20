-- Add the layer;
INSERT INTO oskari_maplayer(type, name, groupId,
                            opacity, minscale, maxscale, metadataId,
                            url,
                            legend_image,
                            locale)
  VALUES('wmslayer', 'maastokartta_50k', (SELECT MAX(id) FROM oskari_layergroup),
         40, 54000,26000,'c22da116-5095-4878-bb04-dd7db3a1a341',
         'http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms',
         'http://xml.nls.fi/Rasteriaineistot/Merkkienselitykset/2010/01/peruskartta_mk25000.png',
         '{ fi:{name:"Maastokartta 1:50k",subtitle:""},sv:{name:"Terrängkarta 1:50k",subtitle:""},en:{name:"Topographic map 1:50k",subtitle:""}}');

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((SELECT MAX(id) FROM oskari_maplayer),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Addresses%'));


INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'wmslayer+http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms+maastokartta_50k');

-- give view_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '10110');

-- give view_layer, publish and view_published permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '2');
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'PUBLISH', '2');
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_PUBLISHED', '2');
