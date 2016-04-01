INSERT INTO oskari_statistical_layer(datasource_id, layer_id, source_property, layer_property)
VALUES(
(SELECT id FROM oskari_statistical_datasource WHERE locale like '%SotkaNET%'),
(SELECT id FROM oskari_maplayer WHERE name = 'oskari:kunnat2013'),
'kunta','kuntakoodi');


INSERT INTO oskari_statistical_layer(datasource_id, layer_id, source_property, layer_property)
VALUES(
(SELECT id FROM oskari_statistical_datasource WHERE locale like '%KHR%'),
(SELECT id FROM oskari_maplayer WHERE name = 'oskari:kunnat2013'),
'kunta','kuntakoodi');