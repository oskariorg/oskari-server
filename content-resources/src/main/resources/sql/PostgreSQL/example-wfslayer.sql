-- NOTE! Terms of use are listed in here;
-- http://www.suomi.fi/suomifi/tyohuone/yhteiset_palvelut/palvelukartta/palvelukartan_rajapintakuvaus/palvelukartan_aineiston_kayttoehdot/index.html ;
-- THIS IS AN EXAMPLE FOR ADDING WFS LAYER ;

-- add map layer;
INSERT INTO oskari_maplayer(type, name, groupId,
                            minscale, maxscale,
                            url, username, password, version, srs_name, locale)
  VALUES('wfslayer', 'palvelupisteiden_kyselypalvelu', (SELECT MAX(id) FROM oskari_layergroup),
         300000, 1,
         'http://kartta.suomi.fi/geoserver/wfs','','','1.1.0', 'EPSG:3067','{fi:{name:"Palvelupisteiden kyselypalvelu", subtitle:""},sv:{name:"Söktjänst för serviceställen", subtitle:""},en:{name:"Public services query service", subtitle:""}}');

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((SELECT MAX(id) FROM oskari_maplayer),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Population distribution - demography%'));

-- add wfs specific layer data;
  INSERT INTO portti_wfs_layer ( maplayer_id, layer_name, gml_geometry_property, 
    gml_version, gml2_separator, max_features, feature_namespace, wfs_template_model_id, properties, 
    feature_type, selected_feature_params, feature_params_locales, geometry_type, selection_sld_style_id, get_map_tiles, 
    get_feature_info, tile_request, wms_layer_id, feature_element, feature_namespace_uri, geometry_namespace_uri, get_highlight_image, 
    wps_params, tile_buffer)
    VALUES ( (select id from oskari_maplayer where name = 'palvelupisteiden_kyselypalvelu'),
      'palvelupisteiden_kyselypalvelu',
      'the_geom', '3.1.1', false, 1000, 'pkartta', NULL, '', 
       '{}', '{}', '{}', '2d', NULL, true, true, false, NULL,  'toimipaikat', 'www.pkartta.fi', '', true, '{}', '{}');


-- add layer as resource for mapping permissions;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'wfslayer+http://kartta.suomi.fi/geoserver/wfs+palvelupisteiden_kyselypalvelu');

-- give view_layer permission for the resource to guest role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = 'wfslayer+http://kartta.suomi.fi/geoserver/wfs+palvelupisteiden_kyselypalvelu'), 'ROLE', 'VIEW_LAYER', (SELECT id FROM oskari_roles WHERE name = 'Guest'));

-- give view_layer permission for the resource to user role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = 'wfslayer+http://kartta.suomi.fi/geoserver/wfs+palvelupisteiden_kyselypalvelu'), 'ROLE', 'VIEW_LAYER', (SELECT id FROM oskari_roles WHERE name = 'User'));

-- give view_layer permission for the resource to admin role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = 'wfslayer+http://kartta.suomi.fi/geoserver/wfs+palvelupisteiden_kyselypalvelu'), 'ROLE', 'VIEW_LAYER', (SELECT id FROM oskari_roles WHERE name = 'Admin'));

