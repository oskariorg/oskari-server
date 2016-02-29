-- NOTE! Terms of use are listed in here;
-- WFS base layer for analysis - set this layer id into oskari-ext.properties (analysis.baselayer.id)  ;
-- May have a need to fix url and user/pw ;

-- add map layer;
INSERT INTO oskari_maplayer(type, name, groupId,
							minscale, maxscale,
                            opacity,url,username, password, version, srs_name, locale)
  VALUES('wfslayer', 'oskari:analysis_data', (SELECT MAX(id) FROM oskari_layergroup),
  		 15000000, 1,
         50, 'http://localhost:8080/geoserver/oskari/ows', 'admin', 'geoserver','1.1.0','EPSG:4326', '{ fi:{name:"Analyysitaso",subtitle:""},sv:{name:"Analys",subtitle:""},en:{name:"Analyse",subtitle:""}}');


-- add wfs specific layer data;
INSERT INTO portti_wfs_layer (maplayer_id, layer_name, gml_geometry_property, gml_version, gml2_separator, max_features,
 feature_namespace, wfs_template_model_id, properties, feature_type, selected_feature_params, feature_params_locales, geometry_type,
  selection_sld_style_id, get_map_tiles, get_feature_info, tile_request, wms_layer_id, feature_element, feature_namespace_uri, geometry_namespace_uri,
   get_highlight_image, wps_params, tile_buffer)
    VALUES ((select id from oskari_maplayer where name = 'oskari:analysis_data'),
     'oskari:analysis_data', 'geometry', '3.1.1', false,  2000, 'oskari', NULL, NULL, '{}',
      '[]', '{}',
       '2d', NULL, false, true, false, NULL,  'analysis_data', 'http://www.oskari.org', '', true, '{}', '{}');



-- analysis layer is for internal use only so we don't want to add any permissions for it;
