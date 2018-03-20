

-- script to add WFS layer definition to oskari
-- User WFSServiceTester.java to create this script. It uses sql template and
-- fill items found in wfs GetCapabilities request
--


-- DO this first, if admistrative layers group is not yet available

-- INSERT INTO oskari_layergroup(
   --         locale)
    -- VALUES ('{ fi:{name:"$LAYER_GROUP"},sv:{name:"$LAYER_GROUP"},en:{name:"$LAYER_GROUP"}}');



-- add map layer;
INSERT INTO oskari_maplayer(type, name, dataprovider_id,opacity,
                            minscale, maxscale,
                            url, locale)
  VALUES('wfslayer', '$LAYER_NAME', (SELECT id FROM oskari_layergroup WHERE locale LIKE '%$LAYER_GROUP%'),
         $OPACITY, $MIN_SCALE, $MAX_SCALE,
         'wfs', '{ fi:{name:"$FI_LAYER_TITLE",subtitle:""},sv:{name:"$SV_LAYER_TITLE",subtitle:""},en:{name:"$EN_LAYER_TITLE",subtitle:""}}');

-- link to inspire theme;
INSERT INTO oskari_maplayer_group_link(maplayerid,
                                   groupid)
  VALUES((SELECT DISTINCT id FROM oskari_maplayer where name='$LAYER_NAME'),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%$INSPIRE_THEME%'));

-- add wfs specific layer data;
  INSERT INTO portti_wfs_layer ( maplayer_id, layer_name, url, username, password, gml_geometry_property, 
    gml_version, gml2_separator, wfs_version, max_features, feature_namespace, wfs_template_model_id, properties, 
    feature_type, selected_feature_params, feature_params_locales, geometry_type, selection_sld_style_id, get_map_tiles, 
    get_feature_info, tile_request, wms_layer_id, srs_name, feature_element, feature_namespace_uri, geometry_namespace_uri, get_highlight_image, 
    wps_params, tile_buffer) 
    VALUES ( (select distinct id from oskari_maplayer where name = '$LAYER_NAME'),
      '$LAYER_NAME',
       '$WFS_URL', '', '', '$GEOMETRY_PROPERTY', '$GML_VERSION', false, '$WFS_VERSION', $MAXFEATURES, '$NAMESPACE_PREFIX', NULL, '', 
       '{}', '{}', '{}', '2d', NULL, true, true, false, NULL, '$EPSG', '$FEATURE_ELEMENT', '$NAMESPACE_URI', '', true, '$WPS_PARAMS', '{}');

