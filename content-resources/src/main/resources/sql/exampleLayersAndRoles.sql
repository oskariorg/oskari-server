
-- Layer Class;
INSERT INTO portti_layerclass (maplayers_selectable, group_map, locale) values (false, false, '{ fi:{name:"Taustakartat"},sv:{name:"Bakgrundskartor"},en:{name:"Background Maps"}}');
INSERT INTO portti_layerclass (maplayers_selectable, group_map, locale, parent) values (false, false, '{ fi:{name:"Taustakartat"},sv:{name:"Bakgrundskartor"},en:{name:"Background Maps"}}',1);
INSERT INTO portti_layerclass (maplayers_selectable, group_map, locale) values (true, false, '{ fi:{name:"Maanmittauslaitos"},sv:{name:"Lantmäteriverket"},en:{name:"National Land Survey"}}');

-- Map Layers;

INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_4m','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',2834657,1417333,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:4milj",subtitle:""},sv:{name:"Bakgrundskarta 1:4milj",subtitle:""},en:{name:"Background map 1:4mill",subtitle:""}}');


INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_5k','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',5000,1,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:5000",subtitle:""},sv:{name:"Bakgrundskarta 1:5000",subtitle:""},en:{name:"Background map 1:5000",subtitle:""}}');


INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_8m','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',15000000,2834657,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:8milj",subtitle:""},sv:{name:"Bakgrundskarta 1:8milj",subtitle:""},en:{name:"Background map 1:8mill",subtitle:""}}');


INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_20k','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',40001,25001,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:20k",subtitle:""},sv:{name:"Bakgrundskarta 1:20k",subtitle:""},en:{name:"Background map 1:20k",subtitle:""}}');


INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_800k','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',566939,283474,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:800k",subtitle:""},sv:{name:"Bakgrundskarta 1:800k",subtitle:""},en:{name:"Background map 1:800k",subtitle:""}}');


INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_2m','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',1417333,566939,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:2milj",subtitle:""},sv:{name:"Bakgrundskarta 1:2milj",subtitle:""},en:{name:"Background map 1:2mill",subtitle:""}}');


INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_320k','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',283474,141742,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:320k",subtitle:""},sv:{name:"Bakgrundskarta 1:320k",subtitle:""},en:{name:"Background map 1:320k",subtitle:""}}');

INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_160k','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',141742,56702,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:160k",subtitle:""},sv:{name:"Bakgrundskarta 1:160k",subtitle:""},en:{name:"Background map 1:160k",subtitle:""}}');

INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_10k','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',25001,5001,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:10k",subtitle:""},sv:{name:"Bakgrundskarta 1:10k",subtitle:""},en:{name:"Background map 1:10k",subtitle:""}}');


INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_80k','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',56702,40000,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:80k",subtitle:""},sv:{name:"Bakgrundskarta 1:80k",subtitle:""},en:{name:"Background map 1:80k",subtitle:""}}');


INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id, 
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (2,'taustakartta_40k','http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms',100,'',2,1,'','',1,'c22da116-5095-4878-bb04-dd7db3a1a341','',2,'wmslayer',
'{ fi:{name:"Taustakartta 1:40k",subtitle:""},sv:{name:"Bakgrundskarta 1:40k",subtitle:""},en:{name:"Background map 1:40k",subtitle:""}}');
   

INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
       style, minscale, maxscale, description_link, legend_image, inspire_theme_id,
       dataurl, metadataurl, order_number, layer_type, locale)
VALUES (3,'peruskartta','http://a.karttatiili.fi/dataset/peruskarttarasteri/service/wms,http://b.karttatiili.fi/dataset/peruskarttarasteri/service/wms,http://c.karttatiili.fi/dataset/peruskarttarasteri/service/wms,http://d.karttatiili.fi/dataset/peruskarttarasteri/service/wms',70,'',25000,1,'','',2,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
'{ fi:{name:"Taustakartta 1:20k",subtitle:""},sv:{name:"Bakgrundskarta 1:20k",subtitle:""},en:{name:"Background map 1:20k",subtitle:""}}');


-- permissions;
-- adding permissions to roles with id 10110, 2, and 3;

-- add layerclasses as resource for mapping permissions (SELECT ('BASE+' || id) as id FROM portti_layerclass);
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('layerclass', 'BASE+1');
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('layerclass', 'BASE+2');
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('layerclass', 'BASE+3');

-- give view_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'layerclass' AND resource_mapping = 'BASE+2'), 'ROLE', 'VIEW_LAYER', '10110');

-- give view_layer permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'layerclass' AND resource_mapping = 'BASE+2'), 'ROLE', 'VIEW_LAYER', '2');

-- give view_layer permission for the resource to ROLE 3 (admin);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'layerclass' AND resource_mapping = 'BASE+2'), 'ROLE', 'VIEW_LAYER', '3');


-- give view_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'layerclass' AND resource_mapping = 'BASE+3'), 'ROLE', 'VIEW_LAYER', '10110');

-- give view_layer permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'layerclass' AND resource_mapping = 'BASE+3'), 'ROLE', 'VIEW_LAYER', '2');

-- give view_layer permission for the resource to ROLE 3 (admin);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'layerclass' AND resource_mapping = 'BASE+3'), 'ROLE', 'VIEW_LAYER', '3');

-- add layer as resource for mapping permissions;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'http://a.karttatiili.fi/dataset/peruskarttarasteri/service/wms,http://b.karttatiili.fi/dataset/peruskarttarasteri/service/wms,http://c.karttatiili.fi/dataset/peruskarttarasteri/service/wms,http://d.karttatiili.fi/dataset/peruskarttarasteri/service/wms+peruskartta');

-- give view_layer permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '2');

-- give view_layer permission for the resource to ROLE 3 (admin);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '3');

-- setup inspire themes;
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Koordinaattijärjestelmät"}, "sv": { "name" : "Referenskoordinatsystem"},"en": { "name" : "Coordinate reference systems"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Paikannusruudustot"}, "sv": { "name" : "Geografiska rutnätssystem"},"en": { "name" : "Geographical grid systems"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Maastokartat"}, "sv": { "name" : "Terrängkartor"},"en": { "name" : "Topographic maps"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Paikannimet"}, "sv": { "name" : "Geografiska namn"},"en": { "name" : "Geographical names"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Hallinnolliset yksiköt"}, "sv": { "name" : "Administrativa enheter"},"en": { "name" : "Administrative units"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Osoitteet"}, "sv": { "name" : "Adresser"},"en": { "name" : "Addresses"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Kiinteistöt"}, "sv": { "name" : "Fastigheter"},"en": { "name" : "Cadastral parcels"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Liikenneverkot"}, "sv": { "name" : "Trafiknät"},"en": { "name" : "Transport networks"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Hydrografia"}, "sv": { "name" : "Hydrografi"},"en": { "name" : "Hydrography"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Suojellut alueet"}, "sv": { "name" : "Skyddade områden"},"en": { "name" : "Protected sites"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Korkeus"}, "sv": { "name" : "Höjd"},"en": { "name" : "Elevation"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Maanpeite"}, "sv": { "name" : "Landtäcke"},"en": { "name" : "Land cover"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Ortoilmakuvat"}, "sv": { "name" : "Ortofoto"},"en": { "name" : "Orthoimagery"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Geologia"}, "sv": { "name" : "Geologi"},"en": { "name" : "Geology"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Korkeus"}, "sv": { "name" : "Höjd"},"en": { "name" : "Elevation"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Tilastoyksiköt"}, "sv": { "name" : "Statistiska enheter"},"en": { "name" : "Statistical units"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Rakennukset"}, "sv": { "name" : "Byggnader"},"en": { "name" : "Buildings"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Maaperä"}, "sv": { "name" : "Mark"},"en": { "name" : "Soil"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Maankäyttö"}, "sv": { "name" : "Markanvändning"},"en": { "name" : "Land use"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Väestön terveys ja turvallisuus"}, "sv": { "name" : "Människors hälsa och säkerhet"},"en": { "name" : "Human health and safety"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Yleishyödylliset ja muut julkiset palvelut"}, "sv": { "name" : "Allmännyttiga och offentliga tjänster"},"en": { "name" : "Utility and governmental services"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Ympäristön tilan seurantalaitteet"}, "sv": { "name" : "Nätverk och anläggningar för miljöövervakning"},"en": { "name" : "Environmental monitoring facilities"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Tuotanto- ja teollisuuslaitokset"}, "sv": { "name" : "Produktions- och industrianläggningar"},"en": { "name" : "Production and industrial facilities"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Maatalous- ja vesiviljelylaitokset"}, "sv": { "name" : "Jordbruks- och vattenbruksanläggningar"},"en": { "name" : "Agricultural and aquaculture facilities"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Väestöjakauma – demografia"}, "sv": { "name" : "Befolkningsfördelning"},"en": { "name" : "Population distribution - demography"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Aluesuunnittelu ja rajoitukset"}, "sv": { "name" : "Områden med särskild reglering"},"en": { "name" : "Area management/restriction zones"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Luonnonriskialueet"}, "sv": { "name" : "Naturliga riskområden"},"en": { "name" : "Natural risk zones"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Ilmakehän tila"}, "sv": { "name" : "Atmosfäriska förhållanden"},"en": { "name" : "Atmospheric conditions"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Ilmaston maantieteelliset ominaispiirteet"}, "sv": { "name" : "Geografiska meteorologiska förhållanden"},"en": { "name" : "Meteorological geographical features"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Merentutkimuksen maantieteelliset ominaispiirteet"}, "sv": { "name" : "Geografiska oceanografiska förhållanden"},"en": { "name" : "Oceanographic geographical features"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Merialueet"}, "sv": { "name" : "Havsområden"},"en": { "name" : "Sea regions"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Biomaantieteelliset alueet"}, "sv": { "name" : "Biogeografiska regioner"},"en": { "name" : "Bio-geographical regions"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Elinympäristöt ja biotoopit"}, "sv": { "name" : "Naturtyper och biotoper"},"en": { "name" : "Habitats and biotopes"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Lajien levinneisyys"}, "sv": { "name" : "Arters utbredning"},"en": { "name" : "Species distribution"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Energiavarat"}, "sv": { "name" : "Energiresurser"},"en": { "name" : "Energy resources"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Mineraalivarat"}, "sv": { "name" : "Mineralfyndigheter"},"en": { "name" : "Mineral resources"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Taustakartat"}, "sv": { "name" : "Bakgrundskartor"},"en": { "name" : "Background maps"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Muut"}, "sv": { "name" : "Andra"},"en": { "name" : "Others"}}');
INSERT INTO portti_inspiretheme (locale) values ('{"fi":{"name":"Opaskartat"}, "sv": { "name" : "Guidekartor"},"en": { "name" : "Guide maps"}}');


-- Add tutorial layers here;
