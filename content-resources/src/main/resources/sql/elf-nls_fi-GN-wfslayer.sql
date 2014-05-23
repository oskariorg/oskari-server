
-- add map layer; 
INSERT INTO oskari_maplayer(type, name, groupId, 
                            minscale, maxscale, 
                            url, locale) 
  VALUES('wfslayer', 'elf_gn_nlsfi', (SELECT MAX(id) FROM oskari_layergroup), 
         120000, 1, 
         'wfs', '{fi:{name:"ELF GN - nls.fi", subtitle:""},sv:{name:"ELF GN - nls.fi", subtitle:""},en:{name:"ELF GN - nls.fi", subtitle:""}}');
         

         
-- link to inspire theme; 
INSERT INTO oskari_maplayer_themes(maplayerid, 
                                   themeid) 
  VALUES((SELECT MAX(id) FROM oskari_maplayer), 
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Geographical names%')); 
         
         
-- add template model stuff 
INSERT INTO portti_wfs_template_model(name, description, type, request_template, response_template) 
VALUES (
	'ELF GN', 'ELF GN PoC', 'mah taip', 
	'/fi/nls/oskari/fe/input/format/gml/inspire/gn/nls_fi_wfs_template.xml', 
	'/fi/nls/oskari/fe/input/format/gml/inspire/gn/ELF_generic_GN.groovy');          

-- add wfs specific layer data; 
INSERT INTO portti_wfs_layer ( 
    maplayer_id, 
    layer_name, 
    url, username, password, 
    gml_geometry_property, gml_version, gml2_separator, 
    wfs_version, max_features, 
    feature_namespace, 
    properties, 
    feature_type, 
    selected_feature_params, 
    feature_params_locales, 
    geometry_type, 
    selection_sld_style_id, get_map_tiles, get_feature_info, tile_request, wms_layer_id, 
    srs_name, 
    feature_element, feature_namespace_uri, 
    geometry_namespace_uri, 
    get_highlight_image, 
    wps_params, 
    tile_buffer, 
    job_type, 
    wfs_template_model_id) 
    VALUES ( (select max(id) from oskari_maplayer), 
      'ELF_GN_nls_fi', 
       '!http://visukarttake01.nls.fi:8080/elf-wfs/services/elf-lod1gn|http://195.156.69.59/elf-wfs/services/elf-lod1gn', '', '', 
       'geom', '3.2.1', false, 
       '2.0.0', 5000, 
       'elf-lod1gn', 
       '', 
       '{"default" : "*geom:Geometry,text:String,script:String,sourceOfName:String,nameStatus:String,nativeness:String,language:String,beginLifespanVersion:String,endLifespanVersion:String,localType:String"}', 
       '{}', 
       '{}', 
       '2d', 
       NULL, true, true, false, NULL, 
	'urn:ogc:def:crs:EPSG::3035', 
	'NamedPlace', 'http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0', 
	'', 
	true, '{}', '{ "default" : 1, "oskari_custom" : 1}', 
	'oskari-feature-engine', (select max(id) from portti_wfs_template_model)); 
	
-- add wfs layer styles; 
INSERT INTO portti_wfs_layer_style (name,sld_style) VALUES(
	'oskari-feature-engine',
	'/fi/nls/oskari/fe/output/style/inspire/gn/nls_fi.xml'
);

-- link wfs layer styles; 
INSERT INTO portti_wfs_layers_styles (wfs_layer_id,wfs_layer_style_id) VALUES(
	(select max(id) from portti_wfs_layer),
	(select max(id) from portti_wfs_layer_style));
	
