INSERT INTO oskari_maplayer(type, name, groupId,
                            opacity,url, locale)
  VALUES('statslayer', 'ows:kunnat2013', (SELECT MAX(id) FROM oskari_layergroup),
         79, '', '{ fi:{name:"SOTKAnet indikaattorit",subtitle:""},sv:{name:"SOTKAnet indikatorer",subtitle:""},en:{name:"SOTKAnet indicators",subtitle:""}}');

insert into portti_stats_layer (maplayer_id, "name", visualization, classes, colors, layername, filterproperty, geometryproperty, externalid) values
((SELECT id FROM oskari_maplayer WHERE type = 'statslayer'),'{ "fi": "otsikko", "en" : "title", "sv" : "title sv"}','choro','','','ows:kunnat2013','kuntakoodi','','sotka:<indicator id>');
