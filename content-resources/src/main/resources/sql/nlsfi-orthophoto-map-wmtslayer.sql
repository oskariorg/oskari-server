-- NOTE! Terms of use are listed in here;
--  ;
-- THIS IS AN EXAMPLE FOR ADDING WMTS LAYER ;

INSERT INTO oskari_maplayer(type, name, groupId,
                            metadataId, url,
                            locale,
                            tile_matrix_set_id)
  VALUES('wmtslayer', 'ortokuva', (select id from oskari_layergroup where locale like '%Maanmittauslaitos%' union select max(id) from oskari_layergroup limit 1),
         'c22da116-5095-4878-bb04-dd7db3a1a341', 'http://karttamoottori.maanmittauslaitos.fi/maasto/wmts',
         '{ fi:{name:"Ortokuvat",subtitle:"(WMTS)"},sv:{name:"Ortofoton",subtitle:"(WMTS)"},en:{name:"Orthophotos",subtitle:"(WMTS)"}}',
         'ETRS-TM35FIN');

-- update tile_matrix for wmts layer;
UPDATE oskari_maplayer SET minScale = 50000, maxScale = 1, tile_matrix_set_data='{
  "operationsMetadata": {
      "GetCapabilities": {
          "dcp": {
              "http": {
                  "get": [
                      {
                          "url": "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts?",
                          "constraints": {
                              "GetEncoding": {
                                  "allowedValues": {
                                      "KVP": true
                                  }
                              }
                          }
                      }
                  ]
              }
          }
      },
      "GetTile": {
          "dcp": {
              "http": {
                  "get": [
                      {
                          "url": "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts?",
                          "constraints": {
                              "GetEncoding": {
                                  "allowedValues": {
                                      "KVP": true
                                  }
                              }
                          }
                      }
                  ]
              }
          }
      },
      "GetFeatureInfo": {
          "dcp": {
              "http": {
                  "get": [
                      {
                          "url": "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts?",
                          "constraints": {
                              "GetEncoding": {
                                  "allowedValues": {
                                      "KVP": true
                                  }
                              }
                          }
                      }
                  ]
              }
          }
      }
  },
  "contents": {
      "layers": [
          {
              "styles": [
                  {
                      "isDefault": true,
                      "identifier": "default"
                  }
              ],
              "formats": ["image/png"],
              "dimensions": [],
              "tileMatrixSetLinks": [
                  {
                      "tileMatrixSet": "ETRS-TM35FIN"
                  }
              ],
              "layers": [],
              "title": "Taustakartta",
              "identifier": "taustakartta",
              "resourceUrl": {
                  "tile": {
                      "format": "image/png",
                      "template": "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/taustakartta/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png",
                      "resourceType": "tile"
                  }
              },
              "resourceUrls": [
                  {
                      "format": "image/png",
                      "template": "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/taustakartta/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png",
                      "resourceType": "tile"
                  }
              ]
          }, {
              "styles": [
                  {
                      "isDefault": true,
                      "identifier": "default"
                  }
              ],
              "formats": ["image/jpeg"],
              "dimensions": [],
              "tileMatrixSetLinks": [
                  {
                      "tileMatrixSet": "ETRS-TM35FIN"
                  }
              ],
              "layers": [],
              "title": "Ortokuva",
              "identifier": "ortokuva",
              "resourceUrl": {
                  "tile": {
                      "format": "image/jpeg",
                      "template": "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/ortokuva/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.jpg",
                      "resourceType": "tile"
                  }
              },
              "resourceUrls": [
                  {
                      "format": "image/jpeg",
                      "template": "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/ortokuva/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.jpg",
                      "resourceType": "tile"
                  }
              ]
          }, {
              "styles": [
                  {
                      "isDefault": true,
                      "identifier": "default"
                  }
              ],
              "formats": ["image/png"],
              "dimensions": [],
              "tileMatrixSetLinks": [
                  {
                      "tileMatrixSet": "ETRS-TM35FIN"
                  }
              ],
              "layers": [],
              "title": "Maastokartta",
              "identifier": "maastokartta",
              "resourceUrl": {
                  "tile": {
                      "format": "image/png",
                      "template": "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/maastokartta/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png",
                      "resourceType": "tile"
                  }
              },
              "resourceUrls": [
                  {
                      "format": "image/png",
                      "template": "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/maastokartta/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png",
                      "resourceType": "tile"
                  }
              ]
          }
      ],
      "tileMatrixSets": {
          "ETRS-TM35FIN": {
              "matrixIds": [
                  {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "0",
                      "scaleDenominator": 29257142.85714286,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 1,
                      "matrixHeight": 1
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "1",
                      "scaleDenominator": 14628571.42857143,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 2,
                      "matrixHeight": 2
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "2",
                      "scaleDenominator": 7314285.714285715,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 4,
                      "matrixHeight": 4
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "3",
                      "scaleDenominator": 3657142.8571428573,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 8,
                      "matrixHeight": 8
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "4",
                      "scaleDenominator": 1828571.4285714286,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 16,
                      "matrixHeight": 16
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "5",
                      "scaleDenominator": 914285.7142857143,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 32,
                      "matrixHeight": 32
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "6",
                      "scaleDenominator": 457142.85714285716,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 64,
                      "matrixHeight": 64
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "7",
                      "scaleDenominator": 228571.42857142858,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 128,
                      "matrixHeight": 128
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "8",
                      "scaleDenominator": 114285.71428571429,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 256,
                      "matrixHeight": 256
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "9",
                      "scaleDenominator": 57142.857142857145,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 512,
                      "matrixHeight": 512
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "10",
                      "scaleDenominator": 28571.428571428572,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 1024,
                      "matrixHeight": 1024
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "11",
                      "scaleDenominator": 14285.714285714286,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 2048,
                      "matrixHeight": 2048
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "12",
                      "scaleDenominator": 7142.857142857143,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 4096,
                      "matrixHeight": 4096
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "13",
                      "scaleDenominator": 3571.4285714285716,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 8192,
                      "matrixHeight": 8192
                  }, {
                      "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
                      "identifier": "14",
                      "scaleDenominator": 1785.7142857142858,
                      "topLeftCorner": {
                          "lon": -548576,
                          "lat": 8388608
                      },
                      "tileWidth": 256,
                      "tileHeight": 256,
                      "matrixWidth": 16384,
                      "matrixHeight": 16384
                  }
              ],
              "identifier": "ETRS-TM35FIN",
              "projection": "urn:ogc:def:crs:EPSG:6.3:3067",
              "bounds": {
                  "left": -548576,
                  "bottom": 6291456,
                  "right": 1548576,
                  "top": 8388608
              },
              "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067"
          }
      }
  },
  "version": "1.0.0"
}' WHERE type='wmtslayer' AND name='ortokuva' AND url='http://karttamoottori.maanmittauslaitos.fi/maasto/wmts';

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((SELECT MAX(id) FROM oskari_maplayer),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Orthoimagery%'));

-- setup permissions for guest user;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'http://karttamoottori.maanmittauslaitos.fi/maasto/wmts+ortokuva');

INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '10110');
