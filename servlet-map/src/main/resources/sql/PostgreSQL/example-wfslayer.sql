-- NOTE! Terms of use are listed in here;
-- http://www.suomi.fi/suomifi/tyohuone/yhteiset_palvelut/palvelukartta/palvelukartan_rajapintakuvaus/palvelukartan_aineiston_kayttoehdot/index.html ;
-- THIS IS AN EXAMPLE FOR ADDING WFS LAYER ;

-- add map layer;
INSERT INTO portti_maplayer(
  layerclassid, wmsname, wmsurl, opacity,
  minscale, maxscale, inspire_theme_id,
  order_number, layer_type, dataurl,
  resource_daily_max_per_ip, "version", epsg, locale)
  VALUES (1, 'palvelupisteiden_kyselypalvelu', 'wfs', 100,
          28347, 1, 1,
          1, 'wfslayer','',
          -1, '1.3.0', 3067, '{fi:{name:"Palvelupisteiden kyselypalvelu", subtitle:""},sv:{name:"Söktjänst för serviceställen", subtitle:""},en:{name:"Public services query service", subtitle:""}}');

-- add wfs specific layer data;
INSERT INTO portti_wfs_layer(
  maplayer_id,
  layer_name,
  url,
  username, "password",
  gml_geometry_property, gml_version, wfs_version, max_features,
  feature_namespace, feature_type, selected_feature_params,
  feature_params_locales, geometry_type,
  get_map_tiles, get_feature_info, srs_name, feature_element,
  feature_namespace_uri, tile_request, gml2_separator)
  VALUES ((select id from portti_maplayer where wmsname = 'palvelupisteiden_kyselypalvelu'),
          'palvelupisteiden_kyselypalvelu',
          'http://kartta.suomi.fi/geoserver/wfs',
          '','',
          'the_geom','3.1.1','1.1.0',100,
          'pkartta','{}','{}',
          '{}','2d',
          true,true,'EPSG:3067','toimipaikat',
          'www.pkartta.fi',false, false);

-- add layer as resource for mapping permissions;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'wfs+palvelupisteiden_kyselypalvelu');

-- give view_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = 'wfs+palvelupisteiden_kyselypalvelu'), 'ROLE', 'VIEW_LAYER', 10110);

