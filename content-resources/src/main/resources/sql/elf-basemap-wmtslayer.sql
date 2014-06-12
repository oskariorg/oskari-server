-- NOTE! Terms of use are listed in here;
--  ;
INSERT INTO oskari_layergroup (id, locale) values (999, '{ fi:{name:"ELF"},sv:{name:"ELF"},en:{name:"ELF"}}');

-- THIS IS AN EXAMPLE FOR ADDING WMTS LAYER ;
INSERT INTO oskari_maplayer(type, name, groupId,
                            metadataId, url,
                            locale,
                            tile_matrix_set_id)
  VALUES('wmtslayer', 'elf_basemap', 999,
         '', 'http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts',
         '{ fi:{name:"ELF taustakartta",subtitle:"(WMTS)"},sv:{name:"ELF Backgrundskarta",subtitle:"(WMTS)"},en:{name:"ELF Background map",subtitle:"(WMTS)"}}',
         'EPSG:3857');

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
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "matrikkel_bakgrunn",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "matrikkel_bakgrunn"
            }, {
                "styles": [
                    {
                        "isDefault": true,
                        "identifier": "default"
                    }
                ],
                "formats": ["image/png", "image/jpeg"],
                "dimensions": [],
                "tileMatrixSetLinks": [
                    {
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:3035"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "topo2",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "topo2"
            }, {
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
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:3035"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "topo2graatone",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "topo2graatone"
            }, {
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
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "Europakart",
                "abstract": "Access Constraints: http://www.mapability.com/info/vmap0_index.html",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "europa"
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
                        "tileMatrixSet": "EPSG:900913"
                    }
                ],
                "layers": [],
                "title": "topo2_direkte",
                "identifier": "topo2_direkte"
            }, {
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
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "toporaster2",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "toporaster2"
            }, {
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
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "sjo_hovedkart2",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "sjo_hovedkart2"
            }, {
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
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "kartdata2",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "kartdata2"
            }, {
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
                        "tileMatrixSet": "EPSG:3034"
                    }
                ],
                "layers": [],
                "title": "ERM",
                "identifier": "ERM"
            }, {
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
                        "tileMatrixSet": "EPSG:3575"
                    }
                ],
                "layers": [],
                "title": "Arcticerm",
                "identifier": "Arcticerm"
            }, {
                "styles": [
                    {
                        "isDefault": true,
                        "identifier": "default"
                    }
                ],
                "formats": ["image/png", "image/jpeg"],
                "dimensions": [],
                "tileMatrixSetLinks": [
                    {
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:3035"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "norges_grunnkart",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "norges_grunnkart"
            }, {
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
                        "tileMatrixSet": "EPSG:32633"
                    }
                ],
                "layers": [],
                "title": "toporaster2_rik",
                "identifier": "toporaster2_rik"
            }, {
                "styles": [
                    {
                        "isDefault": true,
                        "identifier": "default"
                    }
                ],
                "formats": ["image/png", "image/jpeg"],
                "dimensions": [],
                "tileMatrixSetLinks": [
                    {
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:3035"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "norges_grunnkart_graatone",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "norges_grunnkart_graatone"
            }, {
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
                        "tileMatrixSet": "EPSG:3575"
                    }
                ],
                "layers": [],
                "title": "arctic_test",
                "identifier": "arctic_test"
            }, {
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
                        "tileMatrixSet": "EPSG:4258"
                    }, {
                        "tileMatrixSet": "EPSG:3035"
                    }, {
                        "tileMatrixSet": "EPSG:3034"
                    }
                ],
                "layers": [],
                "title": "elf_basemap",
                "identifier": "elf_basemap"
            }, {
                "styles": [
                    {
                        "isDefault": true,
                        "identifier": "default"
                    }
                ],
                "formats": ["image/png", "image/jpeg"],
                "dimensions": [],
                "tileMatrixSetLinks": [
                    {
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:3035"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "Europeiske grunnkart",
                "abstract": "Access Constraints: http://www.eea.europa.eu/data-and-maps/data/european-catchments-and-rivers-network#tab-metadata http://www.gebco.net/data_and_products/gridded_bathymetry_data/documents/gebco_08.pdf http://www.eurogeographics.org/content/eurogeographics-euroglobalmap-opendata",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "egk"
            }, {
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
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "terreng_norgeskart",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "terreng_norgeskart"
            }, {
                "styles": [
                    {
                        "isDefault": true,
                        "identifier": "default"
                    }
                ],
                "formats": ["image/png", "image/jpeg"],
                "dimensions": [],
                "tileMatrixSetLinks": [
                    {
                        "tileMatrixSet": "EPSG:4326"
                    }, {
                        "tileMatrixSet": "EPSG:25832"
                    }, {
                        "tileMatrixSet": "EPSG:25833"
                    }, {
                        "tileMatrixSet": "EPSG:25835"
                    }, {
                        "tileMatrixSet": "EPSG:3035"
                    }, {
                        "tileMatrixSet": "EPSG:900913"
                    }, {
                        "tileMatrixSet": "EPSG:32635"
                    }, {
                        "tileMatrixSet": "EPSG:32633"
                    }, {
                        "tileMatrixSet": "EPSG:32632"
                    }, {
                        "tileMatrixSet": "EPSG:3575"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "Havbunn grunnkart",
                "abstract": "Access Constraints: http://www.eea.europa.eu/data-and-maps/data/european-catchments-and-rivers-network#tab-metadata http://www.gebco.net/data_and_products/gridded_bathymetry_data/documents/gebco_08.pdf http://www.eurogeographics.org/content/eurogeographics-euroglobalmap-opendata",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "havbunn_grunnkart"
            }, {
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
                        "tileMatrixSet": "EPSG:4258"
                    }, {
                        "tileMatrixSet": "EPSG:3035"
                    }, {
                        "tileMatrixSet": "EPSG:3034"
                    }, {
                        "tileMatrixSet": "InspireCRS84Quad"
                    }, {
                        "tileMatrixSet": "EPSG:3857"
                    }
                ],
                "layers": [],
                "title": "elf_basemap",
                "projection": null,
                "bounds": {
                    "left": -180,
                    "bottom": -90,
                    "right": 180,
                    "top": 90
                },
                "identifier": "elf_basemap"
            }
        ],
        "tileMatrixSets": {
            "EPSG:3857": {
                "matrixIds": [
                    {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:0",
                        "scaleDenominator": 559082264.0287179,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 1,
                        "matrixHeight": 1
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:1",
                        "scaleDenominator": 279541132.0143589,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 2,
                        "matrixHeight": 2
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:2",
                        "scaleDenominator": 139770566.0071794,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 4,
                        "matrixHeight": 4
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:3",
                        "scaleDenominator": 69885283.00358972,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 8,
                        "matrixHeight": 8
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:4",
                        "scaleDenominator": 34942641.50179486,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 16,
                        "matrixHeight": 16
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:5",
                        "scaleDenominator": 17471320.75089743,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 32,
                        "matrixHeight": 32
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:6",
                        "scaleDenominator": 8735660.375448715,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 64,
                        "matrixHeight": 64
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:7",
                        "scaleDenominator": 4367830.1877243575,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 128,
                        "matrixHeight": 128
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:8",
                        "scaleDenominator": 2183915.0938621787,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 256,
                        "matrixHeight": 256
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:9",
                        "scaleDenominator": 1091957.5469310887,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 512,
                        "matrixHeight": 512
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:10",
                        "scaleDenominator": 545978.7734655447,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 1024,
                        "matrixHeight": 1024
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:11",
                        "scaleDenominator": 272989.3867327723,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 2048,
                        "matrixHeight": 2048
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:12",
                        "scaleDenominator": 136494.69336638617,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 4096,
                        "matrixHeight": 4096
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:13",
                        "scaleDenominator": 68247.34668319307,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 8192,
                        "matrixHeight": 8192
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:14",
                        "scaleDenominator": 34123.67334159654,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 16384,
                        "matrixHeight": 16384
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:15",
                        "scaleDenominator": 17061.83667079827,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 32768,
                        "matrixHeight": 32768
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:16",
                        "scaleDenominator": 8530.918335399136,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 65536,
                        "matrixHeight": 65536
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:17",
                        "scaleDenominator": 4265.459167699568,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 131072,
                        "matrixHeight": 131072
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:18",
                        "scaleDenominator": 2132.729583849784,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 262144,
                        "matrixHeight": 262144
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:19",
                        "scaleDenominator": 1066.364791924892,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 524288,
                        "matrixHeight": 524288
                    }, {
                        "supportedCRS": "urn:ogc:def:crs:EPSG::3857",
                        "identifier": "EPSG:3857:20",
                        "scaleDenominator": 533.182395962446,
                        "topLeftCorner": {
                            "lon": -20037508.34,
                            "lat": 20037508.34
                        },
                        "tileWidth": 256,
                        "tileHeight": 256,
                        "matrixWidth": 1048576,
                        "matrixHeight": 1048576
                    }
                ],
                "identifier": "EPSG:3857",
                "supportedCRS": "urn:ogc:def:crs:EPSG::3857"
            }
        }
    },
    "serviceMetadataUrl": {
        "href": "http://opencache.statkart.no/geowebcache/service/wmts?REQUEST=getcapabilities&VERSION=1.0.0"
    },
    "version": "1.0.0"
}' WHERE type='wmtslayer' AND name='elf_basemap' AND url='http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts' ;

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((select max(id) from oskari_maplayer),
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

-- give publish permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'PUBLISH', '2');


-- give publish permission for the resource to ROLE 3 (admin);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'PUBLISH', '3');

-- give view_published_layer permission for the resource to ROLE 10110 (guest);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_PUBLISHED', '10110');

-- give view_published_layer permission for the resource to ROLE 2 (user);
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_PUBLISHED', '2');


