-- Cleanup;

DELETE FROM portti_view_bundle_seq;
DELETE FROM portti_bundle;
DELETE FROM portti_view;
DELETE FROM portti_view_supplement;

-- Reset sequences;

--ALTER SEQUENCE portti_bundle_state_id_seq RESTART;
--ALTER SEQUENCE portti_bundle_config_id_seq RESTART;
--ALTER SEQUENCE portti_bundle_id_seq RESTART;
--ALTER SEQUENCE portti_view_id_seq RESTART;
--ALTER SEQUENCE portti_view_supplement_id_seq RESTART;


-- View and supplement;

INSERT INTO portti_view_supplement (app_startup, baseaddress, is_public, creator)
    VALUES ('mapfull', '/Oskari', true, 0);

INSERT INTO portti_view (uuid, name, type, is_default, supplement_id, page, application, application_dev_prefix)
    VALUES ('b6c94ef2-5e15-4cb4-b849-3c5e357f8407',
            'default',
            'DEFAULT',
            true, 
            0,
            'index',
            'servlet',
            '/applications/sample'
    );

-- OpenLayers;

INSERT INTO portti_bundle (name, startup)
       VALUES ('openlayers-default-theme',
'{
    title : "OpenLayers",
    fi : "OpenLayers",
    sv : "?",
    en : "OpenLayers",
    bundlename : "openlayers-default-theme",
    bundleinstancename : "openlayers-default-theme",
    metadata : {
        "Import-Bundle" : {
            "openlayers-single-full" : {
                bundlePath : "/Oskari/packages/openlayers/bundle/"
            },
            "openlayers-default-theme" : {
                bundlePath : "/Oskari/packages/openlayers/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
     },
     instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('openlayers-default-theme',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        1,'{}','{}',
        '{
            title : "OpenLayers",
            fi : "OpenLayers",
            sv : "?",
            en : "OpenLayers",
            bundlename : "openlayers-default-theme",
            bundleinstancename : "openlayers-default-theme",
            metadata : {
                "Import-Bundle" : {
                    "openlayers-single-full" : {
                        bundlePath : "/Oskari/packages/openlayers/bundle/"
                    },
                    "openlayers-default-theme" : {
                        bundlePath : "/Oskari/packages/openlayers/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
             },
             instanceProps : {}
        }'
        );


-- Map;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('mapfull',
'{
     title : "Map",
     fi : "Map",
     sv : "?",
     en : "Map",
     bundlename : "mapfull",
     bundleinstancename : "mapfull",
     metadata : {
         "Import-Bundle" : {
             "core-base" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "core-map" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "sandbox-base" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "sandbox-map" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "event-base" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "event-map" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "event-map-layer" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "request-base" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "request-map" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "request-map-layer" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "service-base" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "service-map" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "domain" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "mapmodule-plugin" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "mapwfs" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "mapwmts" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "mapstats" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "oskariui" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             },
             "mapfull" : {
                 bundlePath : "/Oskari/packages/framework/bundle/"
             }
         },
         "Require-Bundle-Instance" : []
     },
     instanceProps : {}
 }');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('mapfull',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        2,
        '{
            "globalMapAjaxUrl": "[REPLACED BY GETAPP SETUP]",
            "imageLocation": "/Oskari/resources",
            "plugins" : [
               { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin" },
               { "id" : "Oskari.mapframework.mapmodule.WmsLayerPlugin" },
               { "id" : "Oskari.mapframework.mapmodule.MarkersPlugin" },
               { "id" : "Oskari.mapframework.mapmodule.ControlsPlugin" },
               { "id" : "Oskari.mapframework.mapmodule.GetInfoPlugin" },
               { "id" : "Oskari.mapframework.bundle.mapwfs.plugin.wfslayer.WfsLayerPlugin" },
               { "id" : "Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin" } ,
               { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.ScaleBarPlugin" },
               { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar" },
               { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.PanButtons" }
              ],
              "layers": [
                 { "id": "base_2" }
              ]
        }',
        '{
            "east": "517620",
            "north": "6874042",
            "selectedLayers": [{"id": "base_2"}],
            "zoom": 1
        }',
        '{
             title : "Map",
             fi : "Map",
             sv : "?",
             en : "Map",
             bundlename : "mapfull",
             bundleinstancename : "mapfull",
             metadata : {
                 "Import-Bundle" : {
                     "core-base" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "core-map" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "sandbox-base" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "sandbox-map" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "event-base" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "event-map" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "event-map-layer" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "request-base" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "request-map" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "request-map-layer" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "service-base" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "service-map" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "domain" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "mapmodule-plugin" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "mapwfs" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "mapwmts" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "mapstats" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "oskariui" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     },
                     "mapfull" : {
                         bundlePath : "/Oskari/packages/framework/bundle/"
                     }
                 },
                 "Require-Bundle-Instance" : []
             },
             instanceProps : {}
         }');


-- Oskari DIV Manazer;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('divmanazer',
'{
    title : "Oskari DIV Manazer",
    fi : "Oskari DIV Manazer",
    sv : "?",
    en : "Oskari DIV Manazer",
    bundlename : "divmanazer",
    bundleinstancename : "divmanazer",
    metadata : {
        "Import-Bundle" : {
            "divmanazer" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('divmanazer',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        3,'{}','{}',
        '{
            title : "Oskari DIV Manazer",
            fi : "Oskari DIV Manazer",
            sv : "?",
            en : "Oskari DIV Manazer",
            bundlename : "divmanazer",
            bundleinstancename : "divmanazer",
            metadata : {
                "Import-Bundle" : {
                    "divmanazer" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- Toolbar;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('toolbar',
'{
    title : "Toolbar",
    fi : "toolbar",
    sv : "?",
    en : "?",
    bundlename : "toolbar",
    bundleinstancename : "toolbar",
    metadata : {
        "Import-Bundle" : {
            "toolbar" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
             }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('toolbar',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        4,'{
          "viewtools": {
              "print" : false
          }
        }','{}',
        '{
            title : "Toolbar",
            fi : "toolbar",
            sv : "?",
            en : "?",
            bundlename : "toolbar",
            bundleinstancename : "toolbar",
            metadata : {
                "Import-Bundle" : {
                    "toolbar" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                     }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- StateHandler;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('statehandler',
'{
    title : "StateHandler",
    fi : "jquery",
    sv : "?",
    en : "?",
    bundlename : "statehandler",
    bundleinstancename : "statehandler",
    metadata : {
        "Import-Bundle" : {
            "statehandler" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('statehandler',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        5,'{}','{}',
        '{
            title : "StateHandler",
            fi : "jquery",
            sv : "?",
            en : "?",
            bundlename : "statehandler",
            bundleinstancename : "statehandler",
            metadata : {
                "Import-Bundle" : {
                    "statehandler" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- Info Box;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('infobox',
'{
    title : "Info Box",
    fi : "infobox",
    sv : "?",
    en : "?",
    bundlename : "infobox",
    bundleinstancename : "infobox",
    metadata : {
        "Import-Bundle" : {
            "infobox" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('infobox',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        6,'{
          "adaptable": true
        }','{}',
        '{
            title : "Info Box",
            fi : "infobox",
            sv : "?",
            en : "?",
            bundlename : "infobox",
            bundleinstancename : "infobox",
            metadata : {
                "Import-Bundle" : {
                    "infobox" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- Search;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('search',
'{
    title : "Haku",
    fi : "search",
    sv : "?",
    en : "?",
    bundlename : "search",
    bundleinstancename : "search",
    metadata : {
        "Import-Bundle" : {
            "search" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('search',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        7,'{}','{}',
        '{
            title : "Haku",
            fi : "search",
            sv : "?",
            en : "?",
            bundlename : "search",
            bundleinstancename : "search",
            metadata : {
                "Import-Bundle" : {
                    "search" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- Layer Selector ;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('layerselector2',
'{
    title : "Karttatasot",
    fi : "layerselector",
    sv : "?",
    en : "?",
    bundlename : "layerselector2",
    bundleinstancename : "layerselector2",
    metadata : {
        "Import-Bundle" : {
            "layerselector2" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('layerselector2',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        8,'{}','{}',
        '{
            title : "Karttatasot",
            fi : "layerselector",
            sv : "?",
            en : "?",
            bundlename : "layerselector2",
            bundleinstancename : "layerselector2",
            metadata : {
                "Import-Bundle" : {
                    "layerselector2" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- Layer Selection;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('layerselection2',
'{
    title : "Valitut karttatasot",
    fi : "layerselection",
    sv : "?",
    en : "?",
    bundlename : "layerselection2",
    bundleinstancename : "layerselection2",
    metadata : {
        "Import-Bundle" : {
            "layerselection2" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('layerselection2',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        9,'{}','{}',
        '{
            title : "Valitut karttatasot",
            fi : "layerselection",
            sv : "?",
            en : "?",
            bundlename : "layerselection2",
            bundleinstancename : "layerselection2",
            metadata : {
                "Import-Bundle" : {
                    "layerselection2" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- Personal data ;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('personaldata',
'{
    title : "Omat tiedot",
    fi : "personaldata",
    sv : "?",
    en : "?",
    bundlename : "personaldata",
    bundleinstancename : "personaldata",
    metadata : {
        "Import-Bundle" : {
            "personaldata" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
             }
         },
         "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('personaldata',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        10,'{
               "url_changeInfo": "http://www.paikkatietoikkuna.fi/web/fi/",
               "url_changePassword": "http://www.yle.fi",
               "url_removeAccount": "http://www.google.fi"
           }'
           ,'{}',
        '{
            title : "Omat tiedot",
            fi : "personaldata",
            sv : "?",
            en : "?",
            bundlename : "personaldata",
            bundleinstancename : "personaldata",
            metadata : {
                "Import-Bundle" : {
                    "personaldata" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                     }
                 },
                 "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- Publisher;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('publisher',
'{
    title : "Karttajulkaisu",
    fi : "jquery",
    sv : "?",
    en : "?",
    bundlename : "publisher",
    bundleinstancename : "publisher",
    metadata : {
        "Import-Bundle" : {
            "publisher" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('publisher',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        11,'{}','{}',
        '{
            title : "Karttajulkaisu",
            fi : "jquery",
            sv : "?",
            en : "?",
            bundlename : "publisher",
            bundleinstancename : "publisher",
            metadata : {
                "Import-Bundle" : {
                    "publisher" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }'
        );


-- Coordinate Display;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('coordinatedisplay',
'{
    title : "Koordinaattinäyttö",
    fi : "coordinatedisplay",
    sv : "?",
    en : "?",
    bundlename : "coordinatedisplay",
    bundleinstancename : "coordinatedisplay",
    metadata : {
        "Import-Bundle" : {
            "coordinatedisplay" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('coordinatedisplay',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        12,'{}','{}',
        '{
            title : "Koordinaattinäyttö",
            fi : "coordinatedisplay",
            sv : "?",
            en : "?",
            bundlename : "coordinatedisplay",
            bundleinstancename : "coordinatedisplay",
            metadata : {
                "Import-Bundle" : {
                    "coordinatedisplay" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- Metadata Flyout;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('metadataflyout',
'{
    title : "Metadata Flyout",
    fi : "metadataflyout",
    sv : "?",
    en : "?",
    bundlename : "metadataflyout",
    bundleinstancename : "metadataflyout",
    metadata : {
        "Import-Bundle" : {
            "metadataflyout" : {
                bundlePath : "/Oskari/packages/catalogue/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('metadataflyout',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        15,'{}','{}',
        '{
            title : "Metadata Flyout",
            fi : "metadataflyout",
            sv : "?",
            en : "?",
            bundlename : "metadataflyout",
            bundleinstancename : "metadataflyout",
            metadata : {
                "Import-Bundle" : {
                    "metadataflyout" : {
                        bundlePath : "/Oskari/packages/catalogue/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- Featuredata;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('featuredata',
'{
    title : "Featuredata",
    fi : "Kohdetiedot",
    sv : "?",
    en : "?",
    bundlename : "featuredata",
    bundleinstancename : "featuredata",
    metadata : {
        "Import-Bundle" : {
            "featuredata" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('featuredata',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        16,'{}','{}',
        '{
            title : "Featuredata",
            fi : "Kohdetiedot",
            sv : "?",
            en : "?",
            bundlename : "featuredata",
            bundleinstancename : "featuredata",
            metadata : {
                "Import-Bundle" : {
                    "featuredata" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');



-- My places;

INSERT INTO portti_bundle (name, startup) 
    VALUES ('myplaces2',
'{
    title : "My places",
    fi : "Kohteet",
    sv : "Platsar",
    en : "Places",
    bundlename : "myplaces2",
    bundleinstancename : "myplaces2",
    metadata : {
        "Import-Bundle" : {
            "myplaces2" : {
                bundlePath : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    instanceProps : {}
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('myplaces2',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        17,'{
          "queryUrl" : "[REPLACED BY HANDLER]",
          "wmsUrl" : "/karttatiili/myplaces?myCat="
        }','{}',
        '{
            title : "My places",
            fi : "Kohteet",
            sv : "Platsar",
            en : "Places",
            bundlename : "myplaces2",
            bundleinstancename : "myplaces2",
            metadata : {
                "Import-Bundle" : {
                    "myplaces2" : {
                        bundlePath : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            instanceProps : {}
        }');


-- Guided tour;

INSERT INTO portti_bundle (name, startup)
    VALUES ('guidedtour',
'{                    
                    "title": "guidedtour",
                    "bundleinstancename": "guidedtour",
                    "fi": "guidedtour",
                    "sv": "guidedtour",
                    "en": "guidedtour",
                    "bundlename": "guidedtour",
                    "metadata": {
                        "Import-Bundle": {
                            "guidedtour": {
                                "bundlePath": "/Oskari/packages/framework/bundle/"
                            }
                        },
                        "Require-Bundle-Instance": [ ]
                    },
                    "instanceProps": {}
                }
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('guidedtour',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        18,'{}','{}',
        '{
                            "title": "guidedtour",
                            "bundleinstancename": "guidedtour",
                            "fi": "guidedtour",
                            "sv": "guidedtour",
                            "en": "guidedtour",
                            "bundlename": "guidedtour",
                            "metadata": {
                                "Import-Bundle": {
                                    "guidedtour": {
                                        "bundlePath": "/Oskari/packages/framework/bundle/"
                                    }
                                },
                                "Require-Bundle-Instance": [ ]
                            },
                            "instanceProps": {}
                        }
        }'
        );



-- Map Legend;

INSERT INTO portti_bundle (name, startup)
    VALUES ('maplegend',
'{                    
                    "title": "maplegend",
                    "bundleinstancename": "maplegend",
                    "fi": "maplegend",
                    "sv": "maplegend",
                    "en": "maplegend",
                    "bundlename": "maplegend",
                    "metadata": {
                        "Import-Bundle": {
                            "maplegend": {
                                "bundlePath": "/Oskari/packages/framework/bundle/"
                            }
                        },
                        "Require-Bundle-Instance": [ ]
                    },
                    "instanceProps": {}
                }
}');

INSERT INTO portti_view_bundle_seq (bundleinstance, view_id, bundle_id, seqno, config, state, startup)
    VALUES  ('maplegend',
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        13,'{}','{}',
        '{
                            "title": "maplegend",
                            "bundleinstancename": "maplegend",
                            "fi": "maplegend",
                            "sv": "maplegend",
                            "en": "maplegend",
                            "bundlename": "maplegend",
                            "metadata": {
                                "Import-Bundle": {
                                    "maplegend": {
                                        "bundlePath": "/Oskari/packages/framework/bundle/"
                                    }
                                },
                                "Require-Bundle-Instance": [ ]
                            },
                            "instanceProps": {}
                        }
        }');

-- Backend Status;


INSERT INTO portti_bundle (name, startup)
  VALUES ('backendstatus',
          '{
                              "title": "backendstatus",
                              "bundleinstancename": "backendstatus",
                              "fi": "backendstatus",
                              "sv": "backendstatus",
                              "en": "backendstatus",
                              "bundlename": "backendstatus",
                              "metadata": {
                                  "Import-Bundle": {
                                      "backendstatus": {
                                          "bundlePath": "/Oskari/packages/framework/bundle/"
                                      }
                                  },
                                  "Require-Bundle-Instance": [ ]
                              },
                              "instanceProps": {}
                          }
          }');

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES  (
    (SELECT max(id) FROM portti_view WHERE name='default'),
    (SELECT max(id) FROM portti_bundle),
    19,'{}','{}',
    '{
                        "title": "backendstatus",
                        "bundleinstancename": "backendstatus",
                        "fi": "backendstatus",
                        "sv": "backendstatus",
                        "en": "backendstatus",
                        "bundlename": "backendstatus",
                        "metadata": {
                            "Import-Bundle": {
                                "backendstatus": {
                                    "bundlePath": "/Oskari/packages/framework/bundle/"
                                }
                            },
                            "Require-Bundle-Instance": [ ]
                        },
                        "instanceProps": {}
                    }
    }');


-- postprocessor bundle is not currently linked to any view but inserted with code;
-- if a defined parameter is given, so this sql is a bit different from the usual bundle sqls;

INSERT INTO portti_bundle (name, startup)
  VALUES ('postprocessor',
          '{
                              "title": "postprocessor",
                              "bundleinstancename": "postprocessor",
                              "fi": "postprocessor",
                              "sv": "postprocessor",
                              "en": "postprocessor",
                              "bundlename": "postprocessor",
                              "metadata": {
                                  "Import-Bundle": {
                                      "postprocessor": {
                                          "bundlePath": "/Oskari/packages/framework/bundle/"
                                      }
                                  },
                                  "Require-Bundle-Instance": [ ]
                              },
                              "instanceProps": {}
                          }
          }');


