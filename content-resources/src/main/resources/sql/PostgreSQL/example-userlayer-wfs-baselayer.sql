-- NOTE! Terms of use are listed in here;
-- WFS base layer for userlayer - set this layer id into oskari-ext.properties (userlayer.baselayer.id)  ;
-- !!!!!!!  May have a need to fix url and user/pw and geoserver port !!!!!!! ;

-- add map layer;
INSERT INTO oskari_maplayer(type, name, groupId,
							minscale, maxscale,
                            opacity,url,username, password, version, srs_name, locale)
  VALUES('wfslayer', 'oskari:vuser_layer_data', (SELECT MAX(id) FROM oskari_layergroup),
  		 15000000, 1,
         80, 'http://localhost:8080/geoserver/oskari/ows', 'admin', 'geoserver', '1.1.0', 'EPSG:4326', '{ fi:{name:"Omat aineistot",subtitle:""},sv:{name:"User layers",subtitle:""},en:{name:"User layers",subtitle:""}}');


-- add wfs specific layer data;
INSERT INTO portti_wfs_layer (maplayer_id, layer_name, gml_geometry_property, gml_version, gml2_separator,  max_features,
 feature_namespace, wfs_template_model_id, properties, feature_type, selected_feature_params, feature_params_locales, geometry_type,
  selection_sld_style_id, get_map_tiles, get_feature_info, tile_request, wms_layer_id,  feature_element, feature_namespace_uri, geometry_namespace_uri,
   get_highlight_image, wps_params, tile_buffer)
    VALUES ((select id from oskari_maplayer where name = 'oskari:vuser_layer_data'),
     'oskari:vuser_layer_data', 'geometry', '3.1.1', false,  2000, 'oskari', NULL, NULL, '{}',
      '{}', '{}',
       '2d', NULL, false, true, false, NULL,  'vuser_layer_data', 'http://www.oskari.org', '', true, '{}', '{}');


