-- NOTE! Terms of use are listed in here;
-- http://www.suomi.fi/suomifi/tyohuone/yhteiset_palvelut/palvelukartta/palvelukartan_rajapintakuvaus/palvelukartan_aineiston_kayttoehdot/index.html ;
-- THIS IS AN EXAMPLE FOR ADDING WFS LAYER ;

INSERT INTO portti_maplayer(
  layerclassid, wmsname, wmsurl, opacity,
  minscale, maxscale, inspire_theme_id,
  order_number, layer_type, dataurl,
  resource_daily_max_per_ip, "version", epsg, locale)
  VALUES (1, 'palvelupisteiden_kyselypalvelu', 'wfs', 100,
          28347, 1, 1,
          1, 'wfslayer','',
          -1, '1.3.0', 3067, '{fi:{name:"Palvelupisteiden kyselypalvelu", subtitle:""},sv:{name:"Söktjänst för serviceställen", subtitle:""},en:{name:"Public services query service", subtitle:""}}');

INSERT INTO portti_wfs_layer(
  maplayer_id,
  layer_name,
  locales,
  url,
  username, "password",
  gml_geometry_property, gml_version, wfs_version, max_features,
  feature_namespace, feature_type, selected_feature_params,
  feature_params_locales, geometry_type,
  get_map_tiles, get_feature_info, srs_name, feature_element,
  feature_namespace_uri, tile_request, gml2_separator)
  VALUES ((select id from portti_maplayer where wmsname = 'palvelupisteiden_kyselypalvelu'),
          'palvelupisteiden_kyselypalvelu',
          '{fi:{name:"Palvelupisteiden kyselypalvelu", subtitle:""},sv:{name:"Söktjänst för serviceställen", subtitle:""},en:{name:"Public services query service", subtitle:""}}',
          'http://kartta.suomi.fi/geoserver/wfs',
          '','',
          'the_geom','3.1.1','1.1.0',100,
          'pkartta','{}',
          '[]',
          '{}','2d',
          true,true,'EPSG:3067','toimipaikat',
          'www.pkartta.fi',false, false);

INSERT INTO portti_resource_user (resource_name, resource_namespace, resource_type, externalid, externalid_type) values
('palvelupisteiden_kyselypalvelu', 'wfs', 'WMS_LAYER', 10110, 'ROLE');

INSERT INTO portti_permissions (resource_user_id, permissions_type) values ((SELECT MAX(id) FROM portti_resource_user), 'VIEW_LAYER');
