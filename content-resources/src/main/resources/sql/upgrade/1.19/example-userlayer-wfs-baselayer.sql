-- NOTE! Terms of use are listed in here;
-- WFS base layer for userlayer - set this layer id into oskari-ext.properties (userlayer.baselayer.id)  ;
-- !!!!!!!  May have a need to fix url and user/pw  !!!!!!! ;

-- add map layer;
INSERT INTO oskari_maplayer(type, name, groupId,
							minscale, maxscale, 
                            opacity,url, locale)
  VALUES('wfslayer', 'oskari:vuser_layer_data', (SELECT MAX(id) FROM oskari_layergroup),
  		 15000000, 1, 
         80, 'wfs', '{ fi:{name:"Omat aineistot",subtitle:""},sv:{name:"User layers",subtitle:""},en:{name:"User layers",subtitle:""}}');


-- add wfs specific layer data;
INSERT INTO portti_wfs_layer (maplayer_id, layer_name, url, username, password, gml_geometry_property, gml_version, gml2_separator, wfs_version, max_features,
 feature_namespace, wfs_template_model_id, properties, feature_type, selected_feature_params, feature_params_locales, geometry_type,
  selection_sld_style_id, get_map_tiles, get_feature_info, tile_request, wms_layer_id, srs_name, feature_element, feature_namespace_uri, geometry_namespace_uri,
   get_highlight_image, wps_params, tile_buffer, schema_status, custom_parser, test_location, test_zoom)
    VALUES ((select id from oskari_maplayer where name = 'oskari:vuser_layer_data'),
     'oskari:vuser_layer_data', 'http://demo.paikkatietoikkuna.fi/dataset/analysis/service/ows', 'liferay', 'liferay', 'geometry', '3.1.1', false, '1.1.0', 2000, 'oskari', NULL, NULL, '{}',
      '{}', '{}',
       '2d', NULL, false, true, false, NULL, 'EPSG:3067', 'vuser_layer_data', 'http://www.oskari.org', '', true, '{}', '{}', '', false, '[]', 9);


