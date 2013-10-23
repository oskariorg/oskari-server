-- Add a layer under 'National Land Survey' layer class;
INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id,
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (3,'maastokartta_50k','http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms',40,'',54000,26000,'','http://xml.nls.fi/Rasteriaineistot/Merkkienselitykset/2010/01/peruskartta_mk25000.png',3,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Maastokartta 1:50k",subtitle:""},sv:{name:"Terrängkarta 1:50k",subtitle:""},en:{name:"Topographic map 1:50k",subtitle:""}}');

INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms+maastokartta_50k');

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
