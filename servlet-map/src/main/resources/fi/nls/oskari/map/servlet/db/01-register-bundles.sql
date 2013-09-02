-- NOTE!;
-- THE FILE IS TOKENIZED WITH SEMICOLON CHARACTER!;
-- EACH COMMENT _NEED_ TO END WITH A SEMICOLON OR OTHERWISE THE NEXT ACTUAL SQL IS NOT RUN!;
-- ----------------------------------------------------------------------------------------;

-- Cleanup;

DELETE FROM portti_bundle;

-- OpenLayers;

INSERT INTO portti_bundle (name, startup)
  VALUES ('openlayers-default-theme','{}');


UPDATE portti_bundle set startup = '{
    "title" : "OpenLayers",
    "fi" : "OpenLayers",
    "sv" : "OpenLayers",
    "en" : "OpenLayers",
    "bundlename" : "openlayers-default-theme",
    "bundleinstancename" : "openlayers-default-theme",
    "metadata" : {
        "Import-Bundle" : {
            "openlayers-single-full" : {
                "bundlePath" : "/Oskari/packages/openlayers/bundle/"
            },
            "openlayers-default-theme" : {
                "bundlePath" : "/Oskari/packages/openlayers/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
     },
     "instanceProps" : {}
}' WHERE name = 'openlayers-default-theme';

-- Map;

INSERT INTO portti_bundle (name, startup)
  VALUES ('mapfull','{}');


update portti_bundle set startup='{
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
}' where name = 'mapfull';


-- Oskari DIV Manazer;

INSERT INTO portti_bundle (name, startup)
  VALUES ('divmanazer','{}');

UPDATE portti_bundle set startup = '{
    "title": "Oskari DIV Manazer",
    "bundleinstancename": "divmanazer",
    "fi": "divmanazer",
    "sv": "divmanazer",
    "en": "divmanazer",
    "bundlename": "divmanazer",
    "metadata": {
        "Import-Bundle": {
            "divmanazer": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE name = 'divmanazer';



-- Toolbar;

INSERT INTO portti_bundle (name, startup)
  VALUES ('toolbar','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Toolbar",
    "fi" : "toolbar",
    "sv" : "toolbar",
    "en" : "toolbar",
    "bundlename" : "toolbar",
    "bundleinstancename" : "toolbar",
    "metadata" : {
        "Import-Bundle" : {
            "toolbar" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
             }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'toolbar';


-- StateHandler;

INSERT INTO portti_bundle (name, startup)
  VALUES ('statehandler','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Statehandler",
    "fi" : "statehandler",
    "sv" : "statehandler",
    "en" : "statehandler",
    "bundlename" : "statehandler",
    "bundleinstancename" : "statehandler",
    "metadata" : {
        "Import-Bundle" : {
            "statehandler" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'statehandler';

-- Info Box;

INSERT INTO portti_bundle (name, startup)
  VALUES ('infobox','{}');

UPDATE portti_bundle set startup = '{
    "title": "Infobox",
    "bundleinstancename": "infobox",
    "fi": "infobox",
    "sv": "infobox",
    "en": "infobox",
    "bundlename": "infobox",
    "metadata": {
        "Import-Bundle": {
            "infobox": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE name = 'infobox';

-- Search;

INSERT INTO portti_bundle (name, startup)
  VALUES ('search','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Search",
    "fi" : "search",
    "sv" : "?",
    "en" : "?",
    "bundlename" : "search",
    "bundleinstancename" : "search",
    "metadata" : {
        "Import-Bundle" : {
            "search" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'search';

-- Layer Selector ;

INSERT INTO portti_bundle (name, startup)
  VALUES ('layerselector2','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Maplayer selection",
    "fi" : "layerselector2",
    "sv" : "layerselector2",
    "en" : "layerselector2",
    "bundlename" : "layerselector2",
    "bundleinstancename" : "layerselector2",
    "metadata" : {
        "Import-Bundle" : {
            "layerselector2" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'layerselector2';

-- Layer Selection;

INSERT INTO portti_bundle (name, startup)
  VALUES ('layerselection2','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Chosen maplayers",
    "fi" : "layerselection2",
    "sv" : "layerselection2",
    "en" : "layerselection2",
    "bundlename" : "layerselection2",
    "bundleinstancename" : "layerselection2",
    "metadata" : {
        "Import-Bundle" : {
            "layerselection2" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'layerselection2';

-- Personal data ;

INSERT INTO portti_bundle (name, startup)
  VALUES ('personaldata','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Personal data",
    "fi" : "personaldata",
    "sv" : "personaldata",
    "en" : "personaldata",
    "bundlename" : "personaldata",
    "bundleinstancename" : "personaldata",
    "metadata" : {
        "Import-Bundle" : {
            "personaldata" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
             }
         },
         "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'personaldata';

-- Coordinate Display;

INSERT INTO portti_bundle (name, startup)
  VALUES ('coordinatedisplay','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Coordinate display",
    "fi" : "coordinatedisplay",
    "sv" : "coordinatedisplay",
    "en" : "coordinatedisplay",
    "bundlename" : "coordinatedisplay",
    "bundleinstancename" : "coordinatedisplay",
    "metadata" : {
        "Import-Bundle" : {
            "coordinatedisplay" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'coordinatedisplay';

-- Metadata Flyout;

INSERT INTO portti_bundle (name, startup)
  VALUES ('metadataflyout','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Metadata Flyout",
    "fi" : "metadataflyout",
    "sv" : "metadataflyout",
    "en" : "metadataflyout",
    "bundlename" : "metadataflyout",
    "bundleinstancename" : "metadataflyout",
    "metadata" : {
        "Import-Bundle" : {
            "metadataflyout" : {
                "bundlePath" : "/Oskari/packages/catalogue/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'metadataflyout';

-- Featuredata;

INSERT INTO portti_bundle (name, startup)
  VALUES ('featuredata','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Feature data",
    "fi" : "featuredata",
    "sv" : "featuredata",
    "en" : "featuredata",
    "bundlename" : "featuredata",
    "bundleinstancename" : "featuredata",
    "metadata" : {
        "Import-Bundle" : {
            "featuredata" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'featuredata';

-- Map Legend;

INSERT INTO portti_bundle (name, startup)
  VALUES ('maplegend','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Map legend",
    "fi" : "maplegend",
    "sv" : "maplegend",
    "en" : "maplegend",
    "bundlename" : "maplegend",
    "bundleinstancename" : "maplegend",
    "metadata" : {
        "Import-Bundle" : {
            "maplegend" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'maplegend';

-- Backend Status;

INSERT INTO portti_bundle (name, startup)
  VALUES ('backendstatus','{}');


UPDATE portti_bundle set startup = '{
    "title": "Backend status",
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
}' WHERE name = 'backendstatus';


-- postprocessor bundle is not currently linked to any view but inserted with code;
-- if a defined parameter is given, so this sql is a bit different from the usual bundle sqls;

INSERT INTO portti_bundle (name, startup)
  VALUES ('postprocessor','{}');


UPDATE portti_bundle set startup = '{
    "title": "Post processor",
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
}' WHERE name = 'postprocessor';



