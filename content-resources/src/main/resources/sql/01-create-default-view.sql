-- NOTE!;
-- THE FILE IS TOKENIZED WITH SEMICOLON CHARACTER!;
-- EACH COMMENT _NEED_ TO END WITH A SEMICOLON OR OTHERWISE THE NEXT ACTUAL SQL IS NOT RUN!;
-- ----------------------------------------------------------------------------------------;


-- Cleanup;

DELETE FROM portti_view_bundle_seq;
DELETE FROM portti_view;
DELETE FROM portti_view_supplement;

-- View and supplement;
-- TODO: supplement fields are not necessary to keep in own table, the ones still used should be moved to portti_view;
INSERT INTO portti_view_supplement (is_public) VALUES (true);

-- page is the JSP user;
-- application_dev_prefix is the application path prefix under Oskari/applications for development version (non-minified);
-- application is the last part of the application path (common for both minified and dev apps);
INSERT INTO portti_view (name, type, is_default, supplement_id, application, page, application_dev_prefix)
VALUES ('default',
        'DEFAULT',
        true,
        (SELECT max(id) FROM portti_view_supplement),
        'servlet',
        'index',
        '/applications/sample');

-- Start linking bundles;

-- Openlayers;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'openlayers-default-theme'),
          1, '{}','{}', '{}');

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


-- Mapfull;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'mapfull'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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
         AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


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
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.PanButtons" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.FullScreenPlugin" }
      ],
      "layers": [
        { "id": "base_2" }
      ]
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull')
         AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


UPDATE portti_view_bundle_seq set state = '{
  "east": "517620",
  "north": "6874042",
  "selectedLayers": [{"id": "base_2"}],
  "zoom": 1
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull')
         AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


-- Divmanazer;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'divmanazer'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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

-- Toolbar;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'toolbar'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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


UPDATE portti_view_bundle_seq set config = '{
        "viewtools": {
            "print" : false
        }
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'toolbar')
             AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');


-- Statehandler;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'statehandler'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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

-- Infobox;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'infobox'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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


UPDATE portti_view_bundle_seq set config = '{
        "adaptable": true
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'infobox')
             AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- Search;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'search'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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


-- LayerSelector;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'layerselector2'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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

-- LayerSelection;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'layerselection2'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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


-- Coordinate display;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'coordinatedisplay'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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


-- Metadata flyout;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'metadataflyout'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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


-- Feature data;

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'),
          (SELECT id FROM portti_bundle WHERE name = 'featuredata'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
          '{}','{}', '{}');

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
