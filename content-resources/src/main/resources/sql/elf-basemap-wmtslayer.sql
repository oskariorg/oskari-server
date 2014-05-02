-- NOTE! Terms of use are listed in here;
--  ;
INSERT INTO oskari_layergroup (id, locale) values (999, '{ fi:{name:"ELF"},sv:{name:"ELF"},en:{name:"ELF"}}');

-- THIS IS AN EXAMPLE FOR ADDING WMTS LAYER ;
INSERT INTO oskari_maplayer(id, type, name, groupId,
                            metadataId, url,
                            locale,
                            tile_matrix_set_id)
  VALUES(999,'wmtslayer', 'elf_basemap', 999,
         '', 'http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts',
         '{ fi:{name:"ELF taustakartta",subtitle:"(WMTS)"},sv:{name:"ELF Backgrundskarta",subtitle:"(WMTS)"},en:{name:"ELF Background map",subtitle:"(WMTS)"}}',
         'EPSG:3035');

-- update tile_matrix for wmts layer;
UPDATE oskari_maplayer SET tile_matrix_set_data='{
    "serviceIdentification": {
        "title": "Kartverket - Cachetjenester",
        "abstract": "Grunnkart",
        "serviceType": {
            "codeSpace": null,
            "value": "OGC WMTS"
        },
        "serviceTypeVersion": "1.0.0",
        "fees": "None / Norge Digitalt",
        "accessConstraints": "Copyright: Kartverket - see http://www.statkart.no/nor/Land/Kart_og_produkter/visningstjenester/"
    },
    "serviceProvider": {
        "providerName": "Kartverket",
        "providerSite": "http://www.kartverket.no",
        "serviceContact": {
            "individualName": "Tjenestedrift",
            "contactInfo": {
                "phone": {
                    "voice": "08700"
                },
                "address": {
                    "deliveryPoint": "",
                    "city": "Hønefoss",
                    "administrativeArea": "Buskerud",
                    "postalCode": "3507",
                    "country": "Norway",
                    "electronicMailAddress": "wms-drift@kartverket.no"
                }
            }
        }
    },
    "operationsMetadata": {
        "GetCapabilities": {
            "dcp": {
                "http": {
                    "get": [
                        {
                            "url": "http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts?",
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
                            "url": "http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts?",
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
                            "url": "http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts?",
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
                "formats": ["image/jpeg", "image/png"],
                "dimensions": [],
                "tileMatrixSetLinks": [
                    {
                        "tileMatrixSet": "EPSG:3035"
                    }
                ],
                "layers": [],
                "title": "elf_basemap",
                "identifier": "elf_basemap"
            }
        ],
        "tileMatrixSets": {
            "EPSG:3035": {
                "matrixIds": [
                    {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:0",
                        "scaleDenominator": 53956425.89703572,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 1,
                        "matrixHeight": 2
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:1",
                        "scaleDenominator": 26978212.948535718,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 2,
                        "matrixHeight": 3
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:2",
                        "scaleDenominator": 13489106.474250002,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 4,
                        "matrixHeight": 5
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:3",
                        "scaleDenominator": 6744553.237142858,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 8,
                        "matrixHeight": 9
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:4",
                        "scaleDenominator": 3372276.618571429,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 16,
                        "matrixHeight": 17
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:5",
                        "scaleDenominator": 1686138.3092857145,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 32,
                        "matrixHeight": 33
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:6",
                        "scaleDenominator": 843069.1546428573,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 64,
                        "matrixHeight": 65
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:7",
                        "scaleDenominator": 421534.57732142863,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 128,
                        "matrixHeight": 130
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:8",
                        "scaleDenominator": 210767.28864285717,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 256,
                        "matrixHeight": 260
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:9",
                        "scaleDenominator": 105383.64432142858,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 512,
                        "matrixHeight": 519
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:10",
                        "scaleDenominator": 52691.822178571434,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 1024,
                        "matrixHeight": 1038
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:11",
                        "scaleDenominator": 26345.91108214286,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 2048,
                        "matrixHeight": 2075
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:12",
                        "scaleDenominator": 13172.955542857144,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 4096,
                        "matrixHeight": 4150
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:13",
                        "scaleDenominator": 6586.477771428572,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 8192,
                        "matrixHeight": 8300
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:14",
                        "scaleDenominator": 3293.238885714286,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 16384,
                        "matrixHeight": 16600
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:15",
                        "scaleDenominator": 1646.619442857143,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 32768,
                        "matrixHeight": 33199
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:16",
                        "scaleDenominator": 823.3097214285715,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 65536,
                        "matrixHeight": 66398
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:17",
                        "scaleDenominator": 411.65486071428575,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 131072,
                        "matrixHeight": 132795
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:18",
                        "scaleDenominator": 205.82742857142858,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 262144,
                        "matrixHeight": 265589
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3035",
                        "abstract": "The grid was not well-defined, the scale therefore assumes 1m per map unit.",
                        "identifier": "EPSG:3035:19",
                        "scaleDenominator": 102.91371428571429,
                        "topLeftCorner": {
                            "lon": 2426378.0132,
                            "lat": 5446513.5222
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 524288,
                        "matrixHeight": 531177
                    }
                ],
                "identifier": "EPSG:3035",
                "supportedCRS": "urn:ogc:def:crs:EPSG::3035"
            }
        }
    },
    "serviceMetadataUrl": {
        "href": "http://opencache.statkart.no/geowebcache/service/wmts?REQUEST=getcapabilities&VERSION=1.0.0"
    },
    "version": "1.0.0"
}' WHERE type='wmtslayer' AND name='elf_basemap' AND url='http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts' AND id = 999;

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES(999,
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Background maps%'));

-- setup permissions for guest user;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts+elf_basemap');

-- permissions;
-- adding permissions to roles with id 10110, 2, and 3;

-- give view_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '10110');

-- give view_layer permission for the resource to ROLE 1 (guest);
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


