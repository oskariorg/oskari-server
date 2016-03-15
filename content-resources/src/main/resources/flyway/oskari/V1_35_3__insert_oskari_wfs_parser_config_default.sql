-- if table is empty, add default parser for WFS 2
INSERT INTO oskari_wfs_parser_config (name, type, request_template, response_template, parse_config, sld_style)  SELECT 'default', 'Default Path', '/fi/nls/oskari/fe/input/request/wfs/generic/generic_wfs_template.xml', 'fi.nls.oskari.fe.generic.WFS2_DefaultParser', '{
"scan":
{ "scanNS": "http://www.opengis.net/wfs/2.0", "name": "member" }
,
"root":
{ "rootNS": "[via application]", "name": "[via application]" }
,
"paths": [
{ "path": "[via application]", "type": "String", "label": "id" }
]
}', NULL where not exists (select * from oskari_wfs_parser_config);