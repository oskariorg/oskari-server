insert into portti_maplayer (dataurl, layerclassid, namefi, namesv, nameen, wmsname, wmsurl, opacity, minscale, maxscale, inspire_theme_id, layer_type, resource_daily_max_per_ip, epsg,locale) values 
('',37,'SOTKAnet indikaattorit','','','ows:kunnat2013','',79, 15000000, 1, 22, 'statslayer',-1, 3067, '{ fi:{name:"SOTKAnet indikaattorit",subtitle:""},sv:{name:"SOTKAnet indikatorer",subtitle:""},en:{name:"SOTKAnet indicators",subtitle:""}}');

insert into portti_stats_layer (maplayer_id, "name", visualization, classes, colors, layername, filterproperty, geometryproperty, externalid) values 
(<LAYER_ID>,'{ "fi": "otsikko", "en" : "title", "sv" : "title sv"}','choro','','','ows:kunnat2013','kuntakoodi','','sotka:<indicator id>');

