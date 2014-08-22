
-- Map Layers;
INSERT INTO oskari_maplayer(type, name, groupId,
                            url,
                            locale,
                            tile_matrix_set_id)
  VALUES('wmtslayer', 'asdi_basemap', (select id from oskari_layergroup where locale like '%ASDI%' union select max(id) from oskari_layergroup limit 1),
         'http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts',
         '{ "fi": { "name": "Arctic SDI -taustakartta", "subtitle": "Arctic SDI WMTS -taustakartta" }, "sv": { "name": "Arctic SDI bakgrundskarta", "subtitle": "Arctic SDI WMTS bakgrundskarta" }, "en": { "name": "Arctic SDI Background Map", "subtitle": "Arctic SDI WMTS Background Map" }',
         'EPSG:3575_arcticsdi');

-- update tile_matrix for wmts layer;
UPDATE oskari_maplayer SET tile_matrix_set_data='{
   "serviceIdentification": {
      "abstract": "Grunnkart",
      "title": "Kartverket - Cachetjenester",
      "fees": "None / Norge Digitalt",
      "serviceTypeVersion": "1.0.0",
      "serviceType": {
         "codeSpace": null,
         "value": "OGC WMTS"
      },
      "accessConstraints": "Copyright: Kartverket - see http://www.statkart.no/nor/Land/Kart_og_produkter/visningstjenester/"
   },
   "contents": {
      "tileMatrixSets": {
         "EPSG:4326": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 279541132.0143589
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 139770566.00717944
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 69885283.00358972
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 34942641.50179486
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 17471320.75089743
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 8735660.375448715
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 4367830.1877243575
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 2183915.0938621787
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 1091957.5469310894
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 545978.7734655447
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 272989.38673277234
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 136494.69336638617
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 68247.34668319309
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 34123.67334159654
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 17061.83667079827
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 8530.918335399136
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 4265.459167699568
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 262144,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 2132.729583849784
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 524288,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:18",
                  "matrixHeight": 262144,
                  "scaleDenominator": 1066.364791924892
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1048576,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:19",
                  "matrixHeight": 524288,
                  "scaleDenominator": 533.182395962446
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2097152,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:20",
                  "matrixHeight": 1048576,
                  "scaleDenominator": 266.591197981223
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 4194304,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4326:21",
                  "matrixHeight": 2097152,
                  "scaleDenominator": 133.2955989906115
               }
            ],
            "identifier": "EPSG:4326"
         },
         "GlobalCRS84Scale": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 500000000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 3,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 250000000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 6,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:2",
                  "matrixHeight": 3,
                  "scaleDenominator": 100000000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 12,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:3",
                  "matrixHeight": 6,
                  "scaleDenominator": 50000000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 23,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:4",
                  "matrixHeight": 12,
                  "scaleDenominator": 25000000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 56,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:5",
                  "matrixHeight": 28,
                  "scaleDenominator": 10000000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 112,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:6",
                  "matrixHeight": 56,
                  "scaleDenominator": 5000000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 224,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:7",
                  "matrixHeight": 112,
                  "scaleDenominator": 2500000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 560,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:8",
                  "matrixHeight": 280,
                  "scaleDenominator": 1000000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1119,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:9",
                  "matrixHeight": 560,
                  "scaleDenominator": 500000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2237,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:10",
                  "matrixHeight": 1119,
                  "scaleDenominator": 250000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 5591,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:11",
                  "matrixHeight": 2796,
                  "scaleDenominator": 100000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 11182,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:12",
                  "matrixHeight": 5591,
                  "scaleDenominator": 50000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 22364,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:13",
                  "matrixHeight": 11182,
                  "scaleDenominator": 25000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 55909,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:14",
                  "matrixHeight": 27955,
                  "scaleDenominator": 10000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 111817,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:15",
                  "matrixHeight": 55909,
                  "scaleDenominator": 5000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 223633,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:16",
                  "matrixHeight": 111817,
                  "scaleDenominator": 2500
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 559083,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:17",
                  "matrixHeight": 279542,
                  "scaleDenominator": 1000
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1118165,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:18",
                  "matrixHeight": 559083,
                  "scaleDenominator": 500
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2236330,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:19",
                  "matrixHeight": 1118165,
                  "scaleDenominator": 250
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 5590823,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Scale:20",
                  "matrixHeight": 2795412,
                  "scaleDenominator": 100
               }
            ],
            "identifier": "GlobalCRS84Scale"
         },
         "EPSG:4258": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 279541132.0143589
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 139770566.00717944
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 69885283.00358972
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 34942641.50179486
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 17471320.75089743
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 8735660.375448715
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 4367830.1877243575
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 2183915.0938621787
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 1091957.5469310894
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 545978.7734655447
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 272989.38673277234
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 136494.69336638617
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 68247.34668319309
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 34123.67334159654
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 17061.83667079827
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 8530.918335399136
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 4265.459167699568
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 262144,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 2132.729583849784
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 524288,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:18",
                  "matrixHeight": 262144,
                  "scaleDenominator": 1066.364791924892
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 1048576,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:19",
                  "matrixHeight": 524288,
                  "scaleDenominator": 533.182395962446
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 2097152,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:20",
                  "matrixHeight": 1048576,
                  "scaleDenominator": 266.591197981223
               },
               {
                  "topLeftCorner": {
                     "lon": 90,
                     "lat": -180
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4258",
                  "matrixWidth": 4194304,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:4258:21",
                  "matrixHeight": 2097152,
                  "scaleDenominator": 133.2955989906115
               }
            ],
            "identifier": "EPSG:4258"
         },
         "EPSG:25832": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 77371428.57142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 38685714.28571429
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 19342857.142857146
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 9671428.571428573
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 4835714.285714286
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 2417857.142857143
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 1208928.5714285716
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 604464.2857142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 302232.1428571429
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 151116.07142857145
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 75558.03571428572
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 37779.01785714286
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 18889.50892857143
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 9444.754464285716
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 4722.377232142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 2361.188616071429
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 1180.5943080357144
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25832",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25832:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 590.2971540178572
               }
            ],
            "identifier": "EPSG:25832"
         },
         "EPSG:25833": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 77371428.57142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 38685714.28571429
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 19342857.142857146
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 9671428.571428573
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 4835714.285714286
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 2417857.142857143
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 1208928.5714285716
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 604464.2857142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 302232.1428571429
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 151116.07142857145
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 75558.03571428572
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 37779.01785714286
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 18889.50892857143
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 9444.754464285716
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 4722.377232142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 2361.188616071429
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 1180.5943080357144
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25833",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25833:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 590.2971540178572
               }
            ],
            "identifier": "EPSG:25833"
         },
         "GoogleCRS84Quad": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 559082264.0287178
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:1",
                  "matrixHeight": 1,
                  "scaleDenominator": 279541132.0143589
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:2",
                  "matrixHeight": 2,
                  "scaleDenominator": 139770566.0071794
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:3",
                  "matrixHeight": 4,
                  "scaleDenominator": 69885283.00358972
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:4",
                  "matrixHeight": 8,
                  "scaleDenominator": 34942641.50179486
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:5",
                  "matrixHeight": 16,
                  "scaleDenominator": 17471320.75089743
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:6",
                  "matrixHeight": 32,
                  "scaleDenominator": 8735660.375448715
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:7",
                  "matrixHeight": 64,
                  "scaleDenominator": 4367830.187724357
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:8",
                  "matrixHeight": 128,
                  "scaleDenominator": 2183915.093862179
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:9",
                  "matrixHeight": 256,
                  "scaleDenominator": 1091957.546931089
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:10",
                  "matrixHeight": 512,
                  "scaleDenominator": 545978.7734655447
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:11",
                  "matrixHeight": 1024,
                  "scaleDenominator": 272989.3867327723
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:12",
                  "matrixHeight": 2048,
                  "scaleDenominator": 136494.6933663862
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:13",
                  "matrixHeight": 4096,
                  "scaleDenominator": 68247.34668319309
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:14",
                  "matrixHeight": 8192,
                  "scaleDenominator": 34123.67334159654
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:15",
                  "matrixHeight": 16384,
                  "scaleDenominator": 17061.83667079827
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:16",
                  "matrixHeight": 32768,
                  "scaleDenominator": 8530.918335399136
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:17",
                  "matrixHeight": 65536,
                  "scaleDenominator": 4265.459167699568
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 262144,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GoogleCRS84Quad:18",
                  "matrixHeight": 131072,
                  "scaleDenominator": 2132.729583849784
               }
            ],
            "identifier": "GoogleCRS84Quad"
         },
         "EPSG:3575_arcticsdi": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 5,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:0",
                  "matrixHeight": 3,
                  "scaleDenominator": 102400000
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 10,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:1",
                  "matrixHeight": 6,
                  "scaleDenominator": 51200000
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 20,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:2",
                  "matrixHeight": 12,
                  "scaleDenominator": 25600000
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 40,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:3",
                  "matrixHeight": 24,
                  "scaleDenominator": 12800000
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 80,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:4",
                  "matrixHeight": 48,
                  "scaleDenominator": 6400000
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 159,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:5",
                  "matrixHeight": 96,
                  "scaleDenominator": 3200000
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 317,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:6",
                  "matrixHeight": 191,
                  "scaleDenominator": 1600000
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 633,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:7",
                  "matrixHeight": 382,
                  "scaleDenominator": 800000
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 1266,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:8",
                  "matrixHeight": 764,
                  "scaleDenominator": 400000
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 2531,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:9",
                  "matrixHeight": 1527,
                  "scaleDenominator": 200000
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 5061,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575_arcticsdi:10",
                  "matrixHeight": 3053,
                  "scaleDenominator": 100000
               }
            ],
            "identifier": "EPSG:3575_arcticsdi"
         },
         "EPSG:25835": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 77371428.57142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 38685714.28571429
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 19342857.142857146
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 9671428.571428573
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 4835714.285714286
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 2417857.142857143
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 1208928.5714285716
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 604464.2857142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 302232.1428571429
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 151116.07142857145
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 75558.03571428572
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 37779.01785714286
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 18889.50892857143
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 9444.754464285716
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 4722.377232142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 2361.188616071429
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 1180.5943080357144
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::25835",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:25835:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 590.2971540178572
               }
            ],
            "identifier": "EPSG:25835"
         },
         "EPSG:3035": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:0",
                  "matrixHeight": 2,
                  "scaleDenominator": 53956425.89703572
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:1",
                  "matrixHeight": 3,
                  "scaleDenominator": 26978212.948535718
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:2",
                  "matrixHeight": 5,
                  "scaleDenominator": 13489106.474250002
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:3",
                  "matrixHeight": 9,
                  "scaleDenominator": 6744553.237142858
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:4",
                  "matrixHeight": 17,
                  "scaleDenominator": 3372276.618571429
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:5",
                  "matrixHeight": 33,
                  "scaleDenominator": 1686138.3092857145
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:6",
                  "matrixHeight": 65,
                  "scaleDenominator": 843069.1546428573
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:7",
                  "matrixHeight": 130,
                  "scaleDenominator": 421534.57732142863
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:8",
                  "matrixHeight": 260,
                  "scaleDenominator": 210767.28864285717
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:9",
                  "matrixHeight": 519,
                  "scaleDenominator": 105383.64432142858
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:10",
                  "matrixHeight": 1038,
                  "scaleDenominator": 52691.822178571434
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:11",
                  "matrixHeight": 2075,
                  "scaleDenominator": 26345.91108214286
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:12",
                  "matrixHeight": 4150,
                  "scaleDenominator": 13172.955542857144
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:13",
                  "matrixHeight": 8300,
                  "scaleDenominator": 6586.477771428572
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:14",
                  "matrixHeight": 16600,
                  "scaleDenominator": 3293.238885714286
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:15",
                  "matrixHeight": 33199,
                  "scaleDenominator": 1646.619442857143
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:16",
                  "matrixHeight": 66398,
                  "scaleDenominator": 823.3097214285715
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:17",
                  "matrixHeight": 132795,
                  "scaleDenominator": 411.65486071428575
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 262144,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:18",
                  "matrixHeight": 265589,
                  "scaleDenominator": 205.82742857142858
               },
               {
                  "topLeftCorner": {
                     "lon": 2426378.0132,
                     "lat": 5446513.5222
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                  "matrixWidth": 524288,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3035:19",
                  "matrixHeight": 531177,
                  "scaleDenominator": 102.91371428571429
               }
            ],
            "identifier": "EPSG:3035"
         },
         "EPSG:3034": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
            "matrixIds": [
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 58593753.00000001
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 29296876.507142857
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 14648438.253571428
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 7324219.128571429
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 3662109.5642857146
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:5",
                  "matrixHeight": 33,
                  "scaleDenominator": 1831054.7817857144
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:6",
                  "matrixHeight": 65,
                  "scaleDenominator": 915527.3910714287
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:7",
                  "matrixHeight": 129,
                  "scaleDenominator": 457763.69535714283
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:8",
                  "matrixHeight": 257,
                  "scaleDenominator": 228881.84771428574
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:9",
                  "matrixHeight": 513,
                  "scaleDenominator": 114440.92385714287
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:10",
                  "matrixHeight": 1025,
                  "scaleDenominator": 57220.461928571436
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:11",
                  "matrixHeight": 2049,
                  "scaleDenominator": 28610.230964285718
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:12",
                  "matrixHeight": 4098,
                  "scaleDenominator": 14305.115482142859
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:13",
                  "matrixHeight": 8196,
                  "scaleDenominator": 7152.557742857144
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:14",
                  "matrixHeight": 16392,
                  "scaleDenominator": 3576.278871428572
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:15",
                  "matrixHeight": 32783,
                  "scaleDenominator": 1788.139435714286
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:16",
                  "matrixHeight": 65566,
                  "scaleDenominator": 894.069717857143
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:17",
                  "matrixHeight": 131131,
                  "scaleDenominator": 447.03485714285716
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 262144,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:18",
                  "matrixHeight": 262261,
                  "scaleDenominator": 223.51742857142858
               },
               {
                  "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                  "topLeftCorner": {
                     "lon": 2100000.2378,
                     "lat": 5021872.0731
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3034",
                  "matrixWidth": 524288,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3034:19",
                  "matrixHeight": 524522,
                  "scaleDenominator": 111.75871428571429
               }
            ],
            "identifier": "EPSG:3034"
         },
         "InspireCRS84Quad": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 279541132.0143589
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 139770566.00717944
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 69885283.00358972
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 34942641.50179486
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 17471320.75089743
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 8735660.375448715
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 4367830.1877243575
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 2183915.0938621787
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 1091957.5469310894
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 545978.7734655447
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 272989.38673277234
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 136494.69336638617
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 68247.34668319309
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 34123.67334159654
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 17061.83667079827
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 8530.918335399136
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 4265.459167699568
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 262144,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 2132.729583849784
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 524288,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:18",
                  "matrixHeight": 262144,
                  "scaleDenominator": 1066.364791924892
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1048576,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:19",
                  "matrixHeight": 524288,
                  "scaleDenominator": 533.182395962446
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2097152,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:20",
                  "matrixHeight": 1048576,
                  "scaleDenominator": 266.591197981223
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 4194304,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "Inspirecrs84quad:21",
                  "matrixHeight": 2097152,
                  "scaleDenominator": 133.2955989906115
               }
            ],
            "identifier": "InspireCRS84Quad"
         },
         "EPSG:3857": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 559082264.0287179
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 279541132.0143589
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 139770566.0071794
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 69885283.00358972
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 34942641.50179486
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 17471320.75089743
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 8735660.375448715
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 4367830.1877243575
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 2183915.0938621787
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 1091957.5469310887
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 545978.7734655447
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 272989.3867327723
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 136494.69336638617
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 68247.34668319307
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 34123.67334159654
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 17061.83667079827
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 8530.918335399136
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 4265.459167699568
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 262144,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:18",
                  "matrixHeight": 262144,
                  "scaleDenominator": 2132.729583849784
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 524288,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:19",
                  "matrixHeight": 524288,
                  "scaleDenominator": 1066.364791924892
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                  "matrixWidth": 1048576,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3857:20",
                  "matrixHeight": 1048576,
                  "scaleDenominator": 533.182395962446
               }
            ],
            "identifier": "EPSG:3857"
         },
         "EPSG:32635": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 77371428.57142858
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 38685714.28571429
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 19342857.142857146
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 9671428.571428573
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 4835714.285714286
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 2417857.142857143
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 1208928.5714285716
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 604464.2857142858
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 302232.1428571429
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 151116.07142857145
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 75558.03571428572
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 37779.01785714286
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 18889.50892857143
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 9444.754464285716
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 4722.377232142858
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 2361.188616071429
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 1180.5943080357144
               },
               {
                  "topLeftCorner": {
                     "lon": -3500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32635",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32635:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 590.2971540178572
               }
            ],
            "identifier": "EPSG:32635"
         },
         "EPSG:900913": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 559082264.0287179
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 279541132.0143589
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 139770566.0071794
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 69885283.00358972
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 34942641.50179486
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 17471320.75089743
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 8735660.375448715
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 4367830.1877243575
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 2183915.0938621787
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 1091957.5469310887
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 545978.7734655447
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 272989.3867327723
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 136494.69336638617
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 68247.34668319307
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 34123.67334159654
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 17061.83667079827
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 8530.918335399136
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 4265.459167699568
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 262144,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:18",
                  "matrixHeight": 262144,
                  "scaleDenominator": 2132.729583849784
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 524288,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:19",
                  "matrixHeight": 524288,
                  "scaleDenominator": 1066.364791924892
               },
               {
                  "topLeftCorner": {
                     "lon": -20037508.34,
                     "lat": 20037508.34
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::900913",
                  "matrixWidth": 1048576,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:900913:20",
                  "matrixHeight": 1048576,
                  "scaleDenominator": 533.182395962446
               }
            ],
            "identifier": "EPSG:900913"
         },
         "EPSG:32633": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 77371428.57142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 38685714.28571429
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 19342857.142857146
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 9671428.571428573
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 4835714.285714286
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 2417857.142857143
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 1208928.5714285716
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 604464.2857142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 302232.1428571429
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 151116.07142857145
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 75558.03571428572
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 37779.01785714286
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 18889.50892857143
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 9444.754464285716
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 4722.377232142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 2361.188616071429
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 1180.5943080357144
               },
               {
                  "topLeftCorner": {
                     "lon": -2500000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32633",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32633:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 590.2971540178572
               }
            ],
            "identifier": "EPSG:32633"
         },
         "EPSG:32632": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 77371428.57142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 38685714.28571429
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 19342857.142857146
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 9671428.571428573
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 4835714.285714286
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 2417857.142857143
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 1208928.5714285716
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 604464.2857142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 302232.1428571429
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 151116.07142857145
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 75558.03571428572
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 37779.01785714286
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 18889.50892857143
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 9444.754464285716
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 4722.377232142858
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 2361.188616071429
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 1180.5943080357144
               },
               {
                  "topLeftCorner": {
                     "lon": -2000000,
                     "lat": 9045984
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32632",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32632:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 590.2971540178572
               }
            ],
            "identifier": "EPSG:32632"
         },
         "EPSG:3575": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 506011272.50000006
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 253005636.17857143
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:2",
                  "matrixHeight": 3,
                  "scaleDenominator": 126502818.0714286
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:3",
                  "matrixHeight": 5,
                  "scaleDenominator": 63251409.0357143
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:4",
                  "matrixHeight": 10,
                  "scaleDenominator": 31625704.52142857
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:5",
                  "matrixHeight": 20,
                  "scaleDenominator": 15812852.260714285
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:6",
                  "matrixHeight": 39,
                  "scaleDenominator": 7906426.128571429
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:7",
                  "matrixHeight": 78,
                  "scaleDenominator": 3953213.0642857146
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:8",
                  "matrixHeight": 155,
                  "scaleDenominator": 1976606.5325000002
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:9",
                  "matrixHeight": 309,
                  "scaleDenominator": 988303.2664285714
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:10",
                  "matrixHeight": 618,
                  "scaleDenominator": 494151.6332142857
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:11",
                  "matrixHeight": 1236,
                  "scaleDenominator": 247075.8165714286
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:12",
                  "matrixHeight": 2471,
                  "scaleDenominator": 123537.9082857143
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:13",
                  "matrixHeight": 4942,
                  "scaleDenominator": 61768.95414285715
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:14",
                  "matrixHeight": 9883,
                  "scaleDenominator": 30884.477071428573
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:15",
                  "matrixHeight": 19767,
                  "scaleDenominator": 15442.238535714287
               },
               {
                  "topLeftCorner": {
                     "lon": -18133594,
                     "lat": 10776300
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::3575",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:3575:16",
                  "matrixHeight": 39533,
                  "scaleDenominator": 7721.119267857143
               }
            ],
            "identifier": "EPSG:3575"
         },
         "EPSG:32661": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 354352678.5714286
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:1",
                  "matrixHeight": 2,
                  "scaleDenominator": 177176339.2857143
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 4,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:2",
                  "matrixHeight": 4,
                  "scaleDenominator": 88588169.64285715
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 8,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:3",
                  "matrixHeight": 8,
                  "scaleDenominator": 44294084.821428575
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 16,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:4",
                  "matrixHeight": 16,
                  "scaleDenominator": 22147042.410714287
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 32,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:5",
                  "matrixHeight": 32,
                  "scaleDenominator": 11073521.207142858
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 64,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:6",
                  "matrixHeight": 64,
                  "scaleDenominator": 5536760.603571429
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 128,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:7",
                  "matrixHeight": 128,
                  "scaleDenominator": 2768380.3014285713
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 256,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:8",
                  "matrixHeight": 256,
                  "scaleDenominator": 1384190.1507142857
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 512,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:9",
                  "matrixHeight": 512,
                  "scaleDenominator": 692095.0753571428
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 1024,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:10",
                  "matrixHeight": 1024,
                  "scaleDenominator": 346047.5376785714
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 2048,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:11",
                  "matrixHeight": 2048,
                  "scaleDenominator": 173023.7688214286
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 4096,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:12",
                  "matrixHeight": 4096,
                  "scaleDenominator": 86511.88442857144
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 8192,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:13",
                  "matrixHeight": 8192,
                  "scaleDenominator": 43255.94221428572
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 16384,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:14",
                  "matrixHeight": 16384,
                  "scaleDenominator": 21627.97110357143
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 32768,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:15",
                  "matrixHeight": 32768,
                  "scaleDenominator": 10813.98555357143
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 65536,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:16",
                  "matrixHeight": 65536,
                  "scaleDenominator": 5406.992775000001
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 131072,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:17",
                  "matrixHeight": 131072,
                  "scaleDenominator": 2703.4963892857145
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 262144,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:18",
                  "matrixHeight": 262144,
                  "scaleDenominator": 1351.7481928571428
               },
               {
                  "topLeftCorner": {
                     "lon": -10700000,
                     "lat": 14700000
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::32661",
                  "matrixWidth": 524288,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "EPSG:32661:19",
                  "matrixHeight": 524288,
                  "scaleDenominator": 675.8740964285714
               }
            ],
            "identifier": "EPSG:32661"
         },
         "GlobalCRS84Pixel": {
            "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
            "matrixIds": [
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:0",
                  "matrixHeight": 1,
                  "scaleDenominator": 795139219.9519542
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 2,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:1",
                  "matrixHeight": 1,
                  "scaleDenominator": 397569609.9759771
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 3,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:2",
                  "matrixHeight": 2,
                  "scaleDenominator": 198784804.98798856
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 5,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:3",
                  "matrixHeight": 3,
                  "scaleDenominator": 132523203.3253257
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 9,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:4",
                  "matrixHeight": 5,
                  "scaleDenominator": 66261601.66266285
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 17,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:5",
                  "matrixHeight": 9,
                  "scaleDenominator": 33130800.831331424
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 43,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:6",
                  "matrixHeight": 22,
                  "scaleDenominator": 13252320.33253257
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 85,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:7",
                  "matrixHeight": 43,
                  "scaleDenominator": 6626160.166266285
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 169,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:8",
                  "matrixHeight": 85,
                  "scaleDenominator": 3313080.0831331424
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 338,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:9",
                  "matrixHeight": 169,
                  "scaleDenominator": 1656540.0415665712
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1013,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:10",
                  "matrixHeight": 507,
                  "scaleDenominator": 552180.0138555238
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 1688,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:11",
                  "matrixHeight": 844,
                  "scaleDenominator": 331308.00831331423
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 5063,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:12",
                  "matrixHeight": 2532,
                  "scaleDenominator": 110436.00277110476
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 10125,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:13",
                  "matrixHeight": 5063,
                  "scaleDenominator": 55218.00138555238
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 16875,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:14",
                  "matrixHeight": 8438,
                  "scaleDenominator": 33130.80083133143
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 50625,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:15",
                  "matrixHeight": 25313,
                  "scaleDenominator": 11043.600277110474
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 168750,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:16",
                  "matrixHeight": 84375,
                  "scaleDenominator": 3313.080083133142
               },
               {
                  "topLeftCorner": {
                     "lon": -180,
                     "lat": 90
                  },
                  "supportedCRS": "urn:ogc:def:crs:EPSG::4326",
                  "matrixWidth": 506250,
                  "tileHeight": 256,
                  "tileWidth": 256,
                  "identifier": "GlobalCRS84Pixel:17",
                  "matrixHeight": 253125,
                  "scaleDenominator": 1104.3600277110472
               }
            ],
            "identifier": "GlobalCRS84Pixel"
         }
      },
      "layers": [
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "matrikkel_bakgrunn",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "matrikkel_bakgrunn",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:3035"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "topo2",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "topo2",
            "formats": [
               "image/png",
               "image/jpeg"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:3035"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "topo2graatone",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "topo2graatone",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "abstract": "Access Constraints: http://www.mapability.com/info/vmap0_index.html",
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "Europakart",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "europa",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:900913"
               }
            ],
            "title": "topo2_direkte",
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "topo2_direkte",
            "formats": [
               "image/png"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "toporaster2",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "toporaster2",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "sjo_hovedkart2",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "sjo_hovedkart2",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "kartdata2",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "kartdata2",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:3034"
               }
            ],
            "title": "ERM",
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "ERM",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:3575"
               }
            ],
            "title": "Arcticerm",
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "Arcticerm",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:3035"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "norges_grunnkart",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "norges_grunnkart",
            "formats": [
               "image/png",
               "image/jpeg"
            ]
         },
         {
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:32633"
               }
            ],
            "title": "toporaster2_rik",
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "toporaster2_rik",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:3035"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "norges_grunnkart_graatone",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "norges_grunnkart_graatone",
            "formats": [
               "image/png",
               "image/jpeg"
            ]
         },
         {
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:3575_arcticsdi"
               }
            ],
            "title": "arctic_test",
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "arctic_test",
            "formats": [
               "image/png"
            ]
         },
         {
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4258"
               },
               {
                  "tileMatrixSet": "EPSG:3035"
               },
               {
                  "tileMatrixSet": "EPSG:3034"
               }
            ],
            "title": "elf_basemap",
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "elf_basemap",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "abstract": "Access Constraints: http://www.eea.europa.eu/data-and-maps/data/european-catchments-and-rivers-network#tab-metadata; http://www.gebco.net/data_and_products/gridded_bathymetry_data/documents/gebco_08.pdf; http://www.eurogeographics.org/content/eurogeographics-euroglobalmap-opendata",
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:3035"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "Europeiske grunnkart",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "egk",
            "formats": [
               "image/png",
               "image/jpeg"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "terreng_norgeskart",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "terreng_norgeskart",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "abstract": "Access Constraints: http://www.eea.europa.eu/data-and-maps/data/european-catchments-and-rivers-network#tab-metadata; http://www.gebco.net/data_and_products/gridded_bathymetry_data/documents/gebco_08.pdf; http://www.eurogeographics.org/content/eurogeographics-euroglobalmap-opendata",
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4326"
               },
               {
                  "tileMatrixSet": "EPSG:25832"
               },
               {
                  "tileMatrixSet": "EPSG:25833"
               },
               {
                  "tileMatrixSet": "EPSG:25835"
               },
               {
                  "tileMatrixSet": "EPSG:3035"
               },
               {
                  "tileMatrixSet": "EPSG:900913"
               },
               {
                  "tileMatrixSet": "EPSG:32635"
               },
               {
                  "tileMatrixSet": "EPSG:32633"
               },
               {
                  "tileMatrixSet": "EPSG:32632"
               },
               {
                  "tileMatrixSet": "EPSG:3575"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "Havbunn grunnkart",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "havbunn_grunnkart",
            "formats": [
               "image/png",
               "image/jpeg"
            ]
         },
         {
            "bounds": {
               "bottom": -90,
               "left": -180,
               "right": 180,
               "top": 90
            },
            "tileMatrixSetLinks": [
               {
                  "tileMatrixSet": "EPSG:4258"
               },
               {
                  "tileMatrixSet": "EPSG:3035"
               },
               {
                  "tileMatrixSet": "EPSG:3034"
               },
               {
                  "tileMatrixSet": "InspireCRS84Quad"
               },
               {
                  "tileMatrixSet": "EPSG:3857"
               }
            ],
            "title": "elf_basemap",
            "projection": null,
            "layers": [],
            "dimensions": [],
            "styles": [
               {
                  "isDefault": true,
                  "identifier": "default"
               }
            ],
            "identifier": "elf_basemap",
            "formats": [
               "image/jpeg",
               "image/png"
            ]
         }
      ]
   },
   "serviceMetadataUrl": {
      "href": "http://opencache.statkart.no/geowebcache/service/wmts?REQUEST=getcapabilities&VERSION=1.0.0"
   },
   "operationsMetadata": {
      "GetTile": {
         "dcp": {
            "http": {
               "get": [
                  {
                     "constraints": {
                        "GetEncoding": {
                           "allowedValues": {
                              "KVP": true
                           }
                        }
                     },
                     "url": "http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts?"
                  }
               ]
            }
         }
      },
      "GetCapabilities": {
         "dcp": {
            "http": {
               "get": [
                  {
                     "constraints": {
                        "GetEncoding": {
                           "allowedValues": {
                              "KVP": true
                           }
                        }
                     },
                     "url": "http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts?"
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
                     "constraints": {
                        "GetEncoding": {
                           "allowedValues": {
                              "KVP": true
                           }
                        }
                     },
                     "url": "http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts?"
                  }
               ]
            }
         }
      }
   },
   "serviceProvider": {
      "providerSite": "http://www.kartverket.no",
      "providerName": "Kartverket",
      "serviceContact": {
         "contactInfo": {
            "phone": {
               "voice": "08700"
            },
            "address": {
               "postalCode": "3507",
               "deliveryPoint": "",
               "administrativeArea": "Buskerud",
               "electronicMailAddress": "wms-drift@kartverket.no",
               "country": "Norway",
               "city": "Hønefoss"
            }
         },
         "individualName": "Tjenestedrift"
      }
   },
   "version": "1.0.0"
}' WHERE type='wmtslayer' AND name='asdi_basemap' AND url='http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts' ;

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((SELECT MAX(id) FROM oskari_maplayer),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Background maps%'));

-- add layer as resource for mapping permissions;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts+asdi_basemap');

-- permissions;
-- adding permissions to roles with id 10110, 2, and 3;

-- give view_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '10110');

INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '1');

-- give view_layer permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '2');

-- give publish permission for the resource to ROLE 3 (admin);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'PUBLISH', '3');

-- give view_published_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_PUBLISHED', '10110');

-- give view_published_layer permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_PUBLISHED', '2');



-- Add tutorial layers here;
