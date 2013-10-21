
--------------------------------------------
--------------------------------------------
-- Creates a default map view for guest users
-- Notice these statements should be executed in the same order they are listed here
-- for startupsequence to work correctly
--------------------------------------------
--------------------------------------------

---- Checking bundle order ---
SELECT b.name, s.config, s.state, s.startup
    FROM portti_view_bundle_seq s, portti_bundle b 
    WHERE s.bundle_id = b.id AND s.view_id = 
        (SELECT max(v.id) FROM portti_view v, portti_view_supplement s 
            WHERE v.supplement_id = s.id AND s.app_startup = 'digiroad')
    ORDER BY s.view_id, s.seqno;


--------------------------------------------
-- Supplement
-- TODO: This should be refactored so view is inserted first 
-- and supplement should contain some sane values
--   app_startup == js app folder
--   baseaddress == jsp-file
--------------------------------------------

INSERT INTO portti_view_supplement (app_startup, baseaddress, is_public, old_id)
    VALUES ('digiroad', 'view', true, -1);

--------------------------------------------
-- View
--------------------------------------------

INSERT INTO portti_view (name, type, is_default, supplement_id, application, page, application_dev_prefix)
    VALUES ('Guest default view', 
            'USER', 
             false, 
             (SELECT max(id) FROM portti_view_supplement),
             'digiroad',
             'view',
             '/applications');



--------------------------------------------
-- QUERY FOR VIEW ID AND MODIFY THE FOLLOWING STATEMENTS TO USE IT INSTEAD OF [VIEW_ID]
--------------------------------------------

SELECT v.id FROM portti_view v, portti_view_supplement s 
            WHERE v.supplement_id = s.id AND s.app_startup = 'digiroad'


--------------------------------------------
-- 1. Openlayers
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
       VALUES ([VIEW_ID], 
        (SELECT id FROM portti_bundle WHERE name = 'openlayers-default-theme'), 
        1, '{}','{}', '{}', 'openlayers-default-theme');

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
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'openlayers-default-theme') 
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 2. Mapfull
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
       VALUES ([VIEW_ID], 
        (SELECT id FROM portti_bundle WHERE name = 'mapfull'), 
        (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
        '{}','{}', '{}', 'mapfull');

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
            "mapwfs" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "mapwmts" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "mapstats" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "oskariui" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            },
            "mapfull" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') 
    AND view_id=[VIEW_ID];


-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
    "globalMapAjaxUrl": "[REPLACED BY HANDLER]",
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
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.ScaleBarPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.PanButtons" },
       { "id" : "Oskari.mapframework.bundle.mapstats.plugin.StatsLayerPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.FullScreenPlugin" }
      ],
      "layers": [
      ]
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') 
    AND view_id=[VIEW_ID];


-- update proper state for view
UPDATE portti_view_bundle_seq set state = '{
    "selectedLayers": [
        {"id":"base_35","opacity":100},
        {"id":99,"opacity":100},
        {"id":90,"opacity":50}
    ],
    "zoom":11,
    "srs":"EPSG:3067",
    "east":383341,
    "north":6673843
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') 
    AND view_id=[VIEW_ID];



--------------------------------------------
-- 3. Divmanazer
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'divmanazer'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'divmanazer');

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
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 4. Toolbar
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'toolbar'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'toolbar');

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
    AND  view_id=[VIEW_ID];


-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "viewtools": {
            "print" : false
        }
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'toolbar') 
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 5.statehandler
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'statehandler'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'statehandler');

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
    AND  view_id=[VIEW_ID];


-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "logUrl" : "/log/maplink.png"
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'statehandler') 
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 6. Infobox
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'infobox'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'infobox');

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
    AND  view_id=[VIEW_ID];


-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "adaptable": true
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'infobox') 
    AND view_id=[VIEW_ID];


--------------------------------------------
-- 7. Search
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'search'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'search');

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
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 8. LayerSelector
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'layerselector2'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'layerselector2');

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
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 9. LayerSelection
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'layerselection2'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'layerselection2');

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
    AND view_id=[VIEW_ID];

	

--------------------------------------------
-- 10. Digiroad Feature selector
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'digiroad-featureselector'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'digiroad-featureselector');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "Valitut kohteet",
        "fi" : "featureselector",
        "sv" : "?",
        "en" : "?",
        "bundlename" : "digiroad-featureselector",
        "bundleinstancename" : "digiroad-featureselector",
        "metadata" : {
            "Import-Bundle" : {
                "digiroad-featureselector" : {
                    "bundlePath" : "/Oskari/packages/digiroad/bundle/"
                }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'digiroad-featureselector') 
    AND view_id=[VIEW_ID];




--------------------------------------------
-- 11. Digiroad Personal data
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'digiroad-personaldata'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'digiroad-personaldata');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "Omat tiedot",
        "fi" : "personaldata",
        "sv" : "?",
        "en" : "?",
        "bundlename" : "digiroad-personaldata",
        "bundleinstancename" : "digiroad-personaldata",
        "metadata" : {
            "Import-Bundle" : {
                "digiroad-personaldata" : {
                    "bundlePath" : "/Oskari/packages/digiroad/bundle/"
                }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'digiroad-personaldata') 
    AND view_id=[VIEW_ID];



	
--------------------------------------------
-- 12. Feature data
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'featuredata'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'featuredata');

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
    AND view_id=[VIEW_ID];

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "selectionTools": false
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'featuredata') 
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 15. Coordinate display
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'coordinatedisplay'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'coordinatedisplay');

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
    AND view_id=[VIEW_ID];

	

--------------------------------------------
-- 16. Digiroad My places
--------------------------------------------

-- add bundle to view 
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'digiroad-myplaces2'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'digiroad-myplaces2');

	

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "My places",
        "fi":"Kohteet",
        "sv":"Platsar",
        "en":"Places",
        "bundlename" : "digiroad-myplaces2",
        "bundleinstancename" : "digiroad-myplaces2",
        "metadata" : {
            "Import-Bundle" : {
                "digiroad-myplaces2" : {
                    "bundlePath" : "/Oskari/packages/digiroad/bundle/"
                }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'digiroad-myplaces2') 
    AND view_id=[VIEW_ID];
	
	
UPDATE portti_view_bundle_seq set config = '{
	 "queryUrl": "http://localhost/wfs",
        "wmsUrl": "http://localhost/wms",
        "snappingLayer": {
            "protocol": "WFS",
            "opts": {
                "srsName": "EPSG:3067",
                "version": "1.1.0",
                "featureType": "LIIKENNE_ELEMENTTI",
                "featureNS": "http://localhost/LiVi",
                "featurePrefix": "LiVi",
                "geometryName": "GEOMETRY",
                "outputFormat": "json"
            }
        }
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'digiroad-myplaces2') 
    AND view_id=[VIEW_ID];


	
--------------------------------------------
-- 16. Map Legend
--------------------------------------------

-- add bundle to view 
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'maplegend'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'maplegend');

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
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 17. User Guide
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'userguide'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'userguide');

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
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 18. Metadata flyout
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'metadataflyout'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'metadataflyout');

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
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 19. Guided tour
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'guidedtour'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'guidedtour');

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
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 20. Backend status
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'backendstatus'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'backendstatus');

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
    AND view_id=[VIEW_ID];



--------------------------------------------
-- 21. Printout
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'printout'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'printout');

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
    AND view_id=[VIEW_ID];

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "backendConfiguration" : { 
            "formatProducers" : { 
                "application/pdf" : "http://wps.paikkatietoikkuna.fi/dataset/map/process/imaging/service/thumbnail/maplink.pdf?", 
                "image/png" : "http://wps.paikkatietoikkuna.fi/dataset/map/process/imaging/service/thumbnail/maplink.png?"  
            } 
        }
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'printout') 
    AND view_id=[VIEW_ID];

--------------------------------------------
-- 22. Stats grid
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
       VALUES ([VIEW_ID], 
        (SELECT id FROM portti_bundle WHERE name = 'statsgrid'), 
        (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
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
    AND  view_id=[VIEW_ID];

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "name": "StatsGrid",
        "sandbox": "sandbox",
        "stateful" : true,
        "viewClazz": "Oskari.statistics.bundle.statsgrid.StatsView"
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'statsgrid') 
    AND  view_id=[VIEW_ID];
