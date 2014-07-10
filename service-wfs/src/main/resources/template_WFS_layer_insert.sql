

-- template to add WFS layer definition to oskari
-- User WFSServiceTester.java to fill this template automaticly
-- 
-- Elements available $WFS_URL?service=WFS&version=1.1.0&request=GetCapabilities
-- add map layer;
-- WFS_URL  wfs service url (e.g. http://geo.stat.fi:8080/geoserver/ows)
-- LAYER_GROUP administrative layer group (e.g. Tilastokeskus)
-- LAYER_NAME
-- OPACITY   (0 -100)
-- MIN_SCALE (smallest scale e.g. 1500000  -- means !:1500000)
-- MAX_SCALE (largest scale e.g. 1 --> means 1:1)
-- FI_LAYER_NAME
-- SV_LAYER_NAME
-- EN_LAYER_NAME
-- INSPIRE_THEME  inspire theme  (e.g. Tilastointiyksiköt)
-- GEOMETRY_PROPERTY  WFS feature default geometry property name (e.g. the_geom)
-- GML_VERSION  e.g. 3.1.1
-- WFS_VERSION  e.g. 1.1.0
-- MAXFEATURES maximum allowed count of features to select in one request - e.g. 20000
-- NAMESPACE_PREFIX  namespace prefix for $FEATURE_ELEMENT  ( e.g. vaestoruutu) 
-- EPSG  spatial reference system code (e.g. EPSG:3067)
-- FEATURE_ELEMENT  name of featuretype
-- NAMESPACE_URI namespace uri of $FEATURE_ELEMENT


-- DO this first, if admistrative layers group is not yet available

-- INSERT INTO oskari_layergroup(
   --         locale)
    -- VALUES ('{ fi:{name:"$LAYER_GROUP"},sv:{name:"$LAYER_GROUP"},en:{name:"$LAYER_GROUP"}}');



-- add map layer;
INSERT INTO oskari_maplayer(type, name, groupId,opacity,
                            minscale, maxscale,
                            url, locale)
  VALUES('wfslayer', '$LAYER_NAME', (SELECT id FROM oskari_layergroup WHERE locale LIKE '%$LAYER_GROUP%'),
         $OPACITY, $MIN_SCALE, $MAX_SCALE,
         'wfs', '{ fi:{name:"$FI_LAYER_NAME",subtitle:""},sv:{name:"$SV_LAYER_NAME",subtitle:""},en:{name:"$EN_LAYER_NAME",subtitle:""}}');

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((SELECT id FROM oskari_maplayer where name='@LAYER_NAME'),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%$INSPIRE_THEME%'));

-- add wfs specific layer data;
  INSERT INTO portti_wfs_layer ( maplayer_id, layer_name, url, username, password, gml_geometry_property, 
    gml_version, gml2_separator, wfs_version, max_features, feature_namespace, wfs_template_model_id, properties, 
    feature_type, selected_feature_params, feature_params_locales, geometry_type, selection_sld_style_id, get_map_tiles, 
    get_feature_info, tile_request, wms_layer_id, srs_name, feature_element, feature_namespace_uri, geometry_namespace_uri, get_highlight_image, 
    wps_params, tile_buffer) 
    VALUES ( (select id from oskari_maplayer where name = '$LAYER_NAME'),
      '$LAYER_NAME',
       '$WFS_URL', '', '', '$GEOMETRY_PROPERTY', '$GML_VERSION', false, '$WFS_VERSION', $MAXFEATURES, '$NAMESPACE_PREFIX', NULL, '', 
       '{}', '{}', '{}', '2d', NULL, true, true, false, NULL, '$EPSG', '$FEATURE_ELEMENT', '$NAMESPACE_URI', '', true, '{}', '{}');

