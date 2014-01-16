-- NOTE! Terms of use are listed in here;
-- WFS base layer for my places - set this layer id into oskari-ext.properties (myplaces.baselayer.id)  ;
--  ;

-- add map layer;
INSERT INTO oskari_maplayer(type, name, groupId,
                            opacity,url, locale)
  VALUES('wfslayer', 'oskari:my_places', (SELECT MAX(id) FROM oskari_layergroup),
         50, 'wfs', '{ fi:{name:"Omat paikat",subtitle:""},sv:{name:"My places",subtitle:""},en:{name:"My places",subtitle:""}}');


-- add wfs specific layer data;
INSERT INTO portti_wfs_layer (maplayer_id, layer_name, url, username, password, gml_geometry_property, gml_version, gml2_separator, wfs_version, max_features,
 feature_namespace, wfs_template_model_id, properties, feature_type, selected_feature_params, feature_params_locales, geometry_type,
  selection_sld_style_id, get_map_tiles, get_feature_info, tile_request, wms_layer_id, srs_name, feature_element, feature_namespace_uri, geometry_namespace_uri,
   get_highlight_image, wps_params, tile_buffer, schema_status, custom_parser, test_location, test_zoom)
    VALUES ((select id from oskari_maplayer where name = 'oskari:my_places'),
     'oskari:my_places', 'http://localhost:8084/geoserver/oskari/ows', 'admin', 'geoserver', 'geometry', '3.1.1', false, '1.1.0', 1000, 'oskari', NULL, '', '{}',
      '{"default": ["name", "place_desc","link", "image_url"],"fi": ["name", "place_desc","link", "image_url"]}', '{ "fi": ["nimi", "kuvaus","linkki", "kuva-linkki"]}',
       '2d', NULL, false, true, false, NULL, 'EPSG:3067', 'my_places', 'http://www.oskari.org', '', true, '{}', '{}', '', false, '[]', 9);



-- add layer as resource for mapping permissions;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'wfs+oskari:my_places');

-- give view_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = 'wfs+oskari:my_places'), 'ROLE', 'VIEW_LAYER', 10110);

-- give view_layer permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = 'wfs+oskari:my_places'), 'ROLE', 'VIEW_LAYER', 2);

-- give view_layer permission for the resource to ROLE 3 (admin);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = 'wfs+oskari:my_places'), 'ROLE', 'VIEW_LAYER', 3);

