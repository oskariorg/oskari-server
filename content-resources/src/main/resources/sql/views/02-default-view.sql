
--------------------------------------------
--------------------------------------------
-- Creates a default map view for logged in users (paikkatietoikkuna.fi)
-- Notice these statements should be executed in the same order they are listed here
-- for startupsequence to work correctly
--------------------------------------------
--------------------------------------------

---- Checking bundle order ---
SELECT b.name, s.config, s.state, s.startup
    FROM portti_view_bundle_seq s, portti_bundle b 
    WHERE s.bundle_id = b.id AND s.view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')
    ORDER BY s.seqno;


--------------------------------------------
-- View
--------------------------------------------

INSERT INTO portti_view (name, type, is_default, application, page, application_dev_prefix)
    VALUES ('default', 
            'DEFAULT', 
            true,
             'full-map',
             'view',
             '/applications/paikkatietoikkuna.fi');

--------------------------------------------
-- 1. Openlayers
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
       VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
        (SELECT id FROM portti_bundle WHERE name = 'openlayers-default-theme'), 
        1, '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
    "title" : "OpenLayers",
    "fi" : "OpenLayers",
    "sv" : "OpenLayers",
    "en" : "OpenLayers",
    "bundlename" : "openlayers-default-theme",
    "bundleinstancename" : "openlayers-default-theme",
    "metadata" : {
        "Import-Bundle" : {
            "openlayers-full-map" : {
                "bundlePath" : "/Oskari/packages/openlayers/bundle/"
            },
            "openlayers-default-theme" : {
                "bundlePath" : "/Oskari/packages/openlayers/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
     },
     "instanceProps" : {}
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'openlayers-default-theme') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 2. Mapfull
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
       VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
        (SELECT id FROM portti_bundle WHERE name = 'mapfull'), 
        (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
        '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
    "title" : "Map",
    "fi" : "mapfull",
    "sv" : "mapfull",
    "en" : "mapfull",
    "bundlename" : "mapfull",
    "bundleinstancename" : "mapfull",
    "metadata" : {
        "Import-Bundle" : {
            "core-base" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "core-map" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "sandbox-base" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "sandbox-map" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "event-base" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "event-map" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "event-map-layer" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "request-base" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "request-map" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "request-map-layer" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "service-base" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "service-map" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "domain" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "mapmodule-plugin" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "mapwfs2" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "mapwmts" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "mapstats" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "mapanalysis" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "mapmyplaces" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "maparcgis" : {
                "bundlePath" : "/Oskari/packages/arcgis/bundle/"
            },
            "oskariui" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "mapfull" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "ui-components": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            },
            "mapuserlayers" : {
              "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
    "globalMapAjaxUrl": "[REPLACED BY HANDLER]",
    "imageLocation": "/Oskari/resources",
    "mapOptions" : {"srsName":"EPSG:3067","maxExtent":{"bottom":6291456,"left":-548576,"right":1548576,"top":8388608},"resolutions":[2048,1024,512,256,128,64,32,16,8,4,2,1,0.5,0.25]},
    "plugins" : [
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.WmsLayerPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.MarkersPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.ControlsPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.GetInfoPlugin",
         "config" : { 
            "ignoredLayerTypes" : ["WFS","MYPLACES", "USERLAYER"],
            "infoBox": false 
         }
       },
       { "id" : "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin", 
         "config" : { 
           "contextPath" : "/transport-0.0.1", 
           "hostname" : "demo.paikkatietoikkuna.fi", 
           "port" : "80",
           "lazy" : true,
           "disconnectTime" : 30000,
           "backoffIncrement": 1000,
           "maxBackoff": 60000,
           "maxNetworkDelay": 10000
         }
       },
       { "id" : "Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin" } ,
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.ScaleBarPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.PanButtons" },
       { "id" : "Oskari.mapframework.bundle.mapmyplaces.plugin.MyPlacesLayerPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapstats.plugin.StatsLayerPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapanalysis.plugin.AnalysisLayerPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.RealtimePlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.FullScreenPlugin" },
       {
            "id" : "Oskari.mapframework.bundle.mapmodule.plugin.BackgroundLayerSelectionPlugin",
            "config" : {
                "showAsDropdown" : false,
                "baseLayers" : ["base_2", "24", "base_35"]
            }
       },
       { 
        "id": "Oskari.mapframework.bundle.mapmodule.plugin.LogoPlugin",
        "config": {
            "mapUrlPrefix": {
                "en": "http://www.paikkatietoikkuna.fi/web/en/map-window?",
                "fi": "http://www.paikkatietoikkuna.fi/web/fi/kartta?",
                "sv": "http://www.paikkatietoikkuna.fi/web/sv/kartfonstret?"
            },
            "termsUrl": {
                "en": "http://www.paikkatietoikkuna.fi/web/en/terms-and-conditions",
                "fi": "http://www.paikkatietoikkuna.fi/web/fi/kayttoehdot",
                "sv": "http://www.paikkatietoikkuna.fi/web/sv/anvandningsvillkor"
            }
        } },
        {"id": "Oskari.mapframework.bundle.myplacesimport.plugin.UserLayersLayerPlugin" },
       { "id" : "Oskari.arcgis.bundle.maparcgis.plugin.ArcGisLayerPlugin" }
      ],
      "layers": [
      ]
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


-- update proper state for view
UPDATE portti_view_bundle_seq set state = '{
    "east": "520000",
    "north": "7250000",
    "selectedLayers": [{"id": "base_35"}],
    "zoom": 0
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


--------------------------------------------
-- 3. Divmanazer
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'divmanazer'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'divmanazer') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 4. Toolbar
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'toolbar'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'toolbar') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
    "viewtools": {
        "print": false
    },
    "basictools": {
        "measureline" : false,
        "measurearea" : false
    },
    "mapUrlPrefix": {
        "en": "http://www.paikkatietoikkuna.fi/web/en/map-window?",
        "fi": "http://www.paikkatietoikkuna.fi/web/fi/kartta?",
        "sv": "http://www.paikkatietoikkuna.fi/web/sv/kartfonstret?"
    }
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'toolbar') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


--------------------------------------------
-- 5.statehandler
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'statehandler'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'statehandler') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "logUrl" : "/log/maplink.png"
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'statehandler') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


--------------------------------------------
-- 6. Infobox
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'infobox'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'infobox') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "adaptable": true
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'infobox') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


--------------------------------------------
-- 7. Search
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'search'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'search') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 8. LayerSelector
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'layerselector2'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'layerselector2') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update proper config for view
UPDATE portti_view_bundle_seq set config='{
   "showSearchSuggestions" : true
}'
WHERE bundle_id = (SELECT id from portti_bundle WHERE name = 'layerselector2') 
AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 9. LayerSelection
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'layerselection2'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'layerselection2') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


--------------------------------------------
-- 10. Personal data
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'personaldata'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'personaldata') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
    "changeInfoUrl": {
        "en": "https://www.paikkatietoikkuna.fi/web/en/profile",
        "fi": "https://www.paikkatietoikkuna.fi/web/fi/profiili",
        "sv": "https://www.paikkatietoikkuna.fi/web/sv/profil"
    },
    "publishedMapUrl": {
        "en": "/published/en/",
        "fi": "/published/fi/",
        "sv": "/published/sv/"
    }
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'personaldata') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 11. Publisher
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'publisher'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "Map publisher",
        "fi" : "publisher",
        "sv" : "publisher",
        "en" : "publisher",
        "bundlename" : "publisher",
        "bundleinstancename" : "publisher",
        "metadata" : {
            "Import-Bundle" : {
                "publisher" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'publisher') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
    "loginUrl": {
        "en": "https://www.paikkatietoikkuna.fi/web/en/login",
        "fi": "https://www.paikkatietoikkuna.fi/web/fi/login",
        "sv": "https://www.paikkatietoikkuna.fi/web/sv/login"
    },
    "registerUrl": {
        "en": "https://www.paikkatietoikkuna.fi/web/en/login?p_p_id=58&p_p_lifecycle=1&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&saveLastPath=0&_58_struts_action=%2Flogin%2Fcreate_account",
        "fi": "https://www.paikkatietoikkuna.fi/web/fi/login?p_p_id=58&p_p_lifecycle=1&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&saveLastPath=0&_58_struts_action=%2Flogin%2Fcreate_account",
        "sv": "https://www.paikkatietoikkuna.fi/web/sv/login?p_p_id=58&p_p_lifecycle=1&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&saveLastPath=0&_58_struts_action=%2Flogin%2Fcreate_account"
    },
    "publishedMapUrl": {
        "en": "www.paikkatietoikkuna.fi/published/en/",
        "fi": "www.paikkatietoikkuna.fi/published/fi/",
        "sv": "www.paikkatietoikkuna.fi/published/sv/"
    },
    "urlPrefix": "www.paikkatietoikkuna.fi"
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'publisher') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 12. Coordinate display
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'coordinatedisplay'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'coordinatedisplay') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 13. Map Legend
--------------------------------------------

-- add bundle to view 
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'maplegend'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'maplegend') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 14. User Guide
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'userguide'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "User Guide",
        "fi" : "userguide",
        "sv" : "userguide",
        "en" : "userguide",
        "bundlename" : "userguide",
        "bundleinstancename" : "userguide",
        "metadata" : {
            "Import-Bundle" : {
                "userguide" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'userguide') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 15. Metadata flyout
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'metadataflyout'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'metadataflyout') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 16. Feature data
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'featuredata'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'featuredata') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "selectionTools": true
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'featuredata') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


--------------------------------------------
-- 17. My Places
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'myplaces2'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "My places",
        "fi" : "Kohteet",
        "sv" : "Platsar",
        "en" : "Places",
        "bundlename" : "myplaces2",
        "bundleinstancename" : "myplaces2",
        "metadata" : {
            "Import-Bundle" : {
                "myplaces2" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'myplaces2') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "queryUrl" : "[REPLACED BY HANDLER]",
        "featureNS" : "http://www.paikkatietoikkuna.fi",
        "wmsUrl" : "/karttatiili/myplaces?myCat="
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'myplaces2') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 18. Guided tour
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'guidedtour'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "Guided Tour",
        "fi" : "guidedtour",
        "sv" : "guidedtour",
        "en" : "guidedtour",
        "bundlename" : "guidedtour",
        "bundleinstancename" : "guidedtour",
        "metadata" : {
            "Import-Bundle" : {
                "guidedtour" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'guidedtour') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 19. Backend status
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'backendstatus'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
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
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'backendstatus') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 20. Printout
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'printout'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title": "Printout",
        "bundleinstancename": "printout",
        "fi": "printout",
        "sv": "printout",
        "en": "printout",
        "bundlename": "printout",
        "metadata": {
            "Import-Bundle": {
                "printout": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                }
            },
            "Require-Bundle-Instance": [ ]
        },
        "instanceProps": {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'printout') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "backendConfiguration" : { 
            "formatProducers" : { 
                "application/pdf" : "http://wps.paikkatietoikkuna.fi/dataset/map/process/imaging/service/thumbnail/maplink.pdf?", 
                "image/png" : "http://wps.paikkatietoikkuna.fi/dataset/map/process/imaging/service/thumbnail/maplink.png?"
            }
        },
         "legend" : {
             "general" : {
                 "legendWidth" : 0.27,
                 "legendRowHeight" : 0.02,
                 "charsInrow" : 32
             },
             "printAreaDefault" : {
                 "strokeColor" : "#00FF00",
                 "strokeOpacity" : 1,
                 "strokeWidth" : 1,
                 "fillColor" : "#FFFFFF",
                 "fillOpacity" : 0.2,
                 "fontColor" : "#000000",
                 "fontSize" : "12px",
                 "fontFamily" : "Liberation Sans",
                 "fontWeight" : "bold"
             },
             "legendBoxDefault" : {
                 "strokeColor" : "#00FF00",
                 "strokeOpacity" : 1,
                 "strokeWidth" : 0,
                 "fillColor" : "#FFFFFF",
                 "fillOpacity" : 0.7,
                 "labelAlign" : "l",
                 "label" : "${name}",
                 "fontColor" : "#000000",
                 "fontSize" : "12px",
                 "fontFamily" : "Liberation Sans",
                 "fontWeight" : "bold"
             },
             "colorBoxDefault" : {
                 "strokeColor" : "#000000",
                 "strokeOpacity" : 1,
                 "strokeWidth" : 1,
                 "fillColor" : "${color}",
                 "fillOpacity" : 1.0,
                 "label" : "${name}",
                 "labelAlign" : "l",
                 "labelXOffset" : 0,
                 "labelYOffset" : 5,
                 "fontFamily" : "Arial",
                 "fontSize" : "10px"
             }
         }
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'printout') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 21. Stats grid
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
       VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
       	(SELECT id FROM portti_bundle WHERE name = 'statsgrid'), 
       	(SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
        '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
    "title": "Statistics grid",
    "bundleinstancename": "statsgrid",
    "fi": "statsgrid",
    "sv": "statsgrid",
    "en": "statsgrid",
    "bundlename": "statsgrid",
    "metadata": {
        "Import-Bundle": {
            "statsgrid": {
                "bundlePath": "/Oskari/packages/statistics/bundle/"
            },
            "geostats": {
                "bundlePath": "/Oskari/packages/libraries/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'statsgrid') 
	AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "name": "StatsGrid",
        "sandbox": "sandbox",
        "stateful" : true,
        "tileClazz": "Oskari.statistics.bundle.statsgrid.Tile",
        "viewClazz": "Oskari.statistics.bundle.statsgrid.StatsView"
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'statsgrid') 
	AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


--------------------------------------------
-- 22. Analyse
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
       VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
        (SELECT id FROM portti_bundle WHERE name = 'analyse'), 
        (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
        '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
    "title": "Analyse",
    "bundleinstancename": "analyse",
    "fi": "analyse",
    "sv": "analyse",
    "en": "analyse",
    "bundlename": "analyse",
    "metadata": {
        "Import-Bundle": {
            "analyse": {
                "bundlePath": "/Oskari/packages/analysis/bundle/"
            },
            "geometryeditor": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'analyse') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 23. Metadata Catalogue
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'metadatacatalogue'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "metadatacatalogue",
        "fi" : "metadatacatalogue",
        "sv" : "metadatacatalogue",
        "en" : "metadatacatalogue",
        "bundlename" : "metadatacatalogue",
        "bundleinstancename" : "metadatacatalogue",
        "metadata" : {
            "Import-Bundle" : {
                "metadatacatalogue" : {
                    "bundlePath" : "/Oskari/packages/catalogue/bundle/"
                }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'metadatacatalogue') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 24. Route Search
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
       VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
        (SELECT id FROM portti_bundle WHERE name = 'routesearch'), 
        (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
        '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
    "title": "Route Search",
    "bundleinstancename": "routesearch",
    "fi": "Reittihaku",
    "sv": "Ruttsök",
    "en": "Route Search",
    "bundlename": "routesearch",
    "metadata": {
        "Import-Bundle": {
            "routesearch": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'routesearch') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
    "flyoutClazz": "Oskari.mapframework.bundle.routesearch.Flyout"
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'routesearch')
         AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

--------------------------------------------
-- 25. FindByCoordinates
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
       VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
        (SELECT id FROM portti_bundle WHERE name = 'findbycoordinates'), 
        (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
        '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
    "title" : "FindByCoordinates",
    "bundlename" : "findbycoordinates",
    "bundleinstancename" : "findbycoordinates",
    "metadata" : {
    "Import-Bundle" : {
    "findbycoordinates" : {
    "bundlePath" : "/Oskari/packages/framework/bundle/"
    }
    },
    "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'findbycoordinates') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

