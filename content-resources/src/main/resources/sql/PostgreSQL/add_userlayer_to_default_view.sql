-- These sql actions are not needed to execute, if you have the newest oskari backend configuration
--
-- add myplacesimport to bundle table

INSERT INTO portti_bundle(
            name, startup, config, state)
    VALUES ( 'myplacesimport', '{
    "title" : "myplacesimport",
    "fi" : "myplacesimport",
    "sv" : "myplacesimport",
    "en" : "myplacesimport",
    "bundlename" : "myplacesimport",
    "bundleinstancename" : "myplacesimport",
    "metadata" : {
        "Import-Bundle" : {
            "myplacesimport" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}', '{
    "name": "MyPlacesImport",
    "sandbox": "sandbox",
    "flyoutClazz": "Oskari.mapframework.bundle.myplacesimport.Flyout"
}','{}');

-- Add myplacesimport bundle (user data import) to default view

INSERT INTO portti_view_bundle_seq(
    view_id, bundle_id, seqno, config, startup, bundleinstance
) VALUES(
    (SELECT id FROM portti_view WHERE type = 'DEFAULT'),
    (SELECT id FROM portti_bundle WHERE name = 'myplacesimport'),
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
    (SELECT config FROM portti_bundle WHERE name = 'myplacesimport'),
    (SELECT startup FROM portti_bundle WHERE name = 'myplacesimport'),
    'myplacesimport'
);

-- Add UserlayersLayerPlugin to default mapfull config
-- Select first your current mapfull config
SELECT view_id, config
  FROM portti_view_bundle_seq where view_id = (SELECT id FROM portti_view WHERE type = 'DEFAULT') and bundle_id=(SELECT id FROM portti_bundle WHERE name = 'mapfull');

-- Update mapfull config 
-- Add to config json string the  UserlayersLayerPlugin section
-- e.g.
-- SELECT result :
-- {"globalMapAjaxUrl":"[REPLACED BY HANDLER]","plugins":[{"id":"Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin"},{"id":"Oskari.mapframework.mapmodule.WmsLayerPlugin"},{"id":"Oskari.mapframework.mapmodule.MarkersPlugin"},{"id":"Oskari.mapframework.mapmodule.ControlsPlugin"},{"id":"Oskari.mapframework.mapmodule.GetInfoPlugin"},{"id":"Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin"},{"id":"Oskari.mapframework.bundle.mapmodule.plugin.ScaleBarPlugin"},{"id":"Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar"},{"id":"Oskari.mapframework.bundle.mapmodule.plugin.PanButtons"},{"id":"Oskari.mapframework.bundle.mapmodule.plugin.FullScreenPlugin"}],"layers":[{"id":1}],"imageLocation":"/Oskari/resources"}

-- Edited config column (UserLayersLayerPlugin inserted):
-- {
-- 	"globalMapAjaxUrl": "[REPLACED BY HANDLER]",
-- 	"plugins": [{
-- 		"id": "Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin"
-- 	}, {
-- 		"id": "Oskari.mapframework.mapmodule.WmsLayerPlugin"
-- 	}, {
-- 		"id": "Oskari.mapframework.mapmodule.MarkersPlugin"
-- 	}, {
-- 		"id": "Oskari.mapframework.mapmodule.ControlsPlugin"
-- 	}, {
-- 		"id": "Oskari.mapframework.mapmodule.GetInfoPlugin"
-- 	}, {
-- 		"id": "Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin"
-- 	}, {
-- 		"id": "Oskari.mapframework.bundle.mapmodule.plugin.ScaleBarPlugin"
-- 	}, {
-- 		"id": "Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar"
-- 	}, {
-- 		"id": "Oskari.mapframework.bundle.mapmodule.plugin.PanButtons"
-- 	}, {
-- 		"id": "Oskari.mapframework.bundle.mapmodule.plugin.FullScreenPlugin"
-- 	}, {
-- 		"id": "Oskari.mapframework.bundle.myplacesimport.plugin.UserLayersLayerPlugin"
-- 	}],
-- 	"layers": [{
-- 		"id": 1
-- 	}],
-- 	"imageLocation": "/Oskari/resources"
-- }

--Update mapfull startup string in portti_view_bundle_seq in default view

UPDATE portti_view_bundle_seq
   SET config=[your edited config] 
 WHERE view_id = (SELECT id FROM portti_view WHERE type = 'DEFAULT') and bundle_id=(SELECT id FROM portti_bundle WHERE name = 'mapfull');

 -- Add UserlayersLayerPlugin to default mapfull startup
-- Select first your current mapfull startup
SELECT view_id, startup
  FROM portti_view_bundle_seq where view_id = (SELECT id FROM portti_view WHERE type = 'DEFAULT') and bundle_id=(SELECT id FROM portti_bundle WHERE name = 'mapfull');

-- Update mapfull startup
-- Add to startup json string the  mapuserlayers section
-- e.g.
-- SELECT result :
-- "{
---   "title" : "Map",
--    "fi" : "mapfull",
--    "sv" : "mapfull",
--    "en" : "mapfull",
--    "bundlename" : "mapfull",
--    "bundleinstancename" : "mapfull",
--    "metadata" : {
--        "Import-Bundle" : {
--            "core-base" : {
--                "bundlePath" : "/Oskari/packages/framework/bundle/"
--            },
--            "core-map" : {
--                "bundlePath" : "/Oskari/packages/framework/bundle/"
--            },
-- ...
--            "oskariui" : {
--                "bundlePath" : "/Oskari/packages/framework/bundle/"
--            },
--            "mapfull" : {
--                "bundlePath" : "/Oskari/packages/framework/bundle/"
--            },
--            "ui-components": {
--                "bundlePath": "/Oskari/packages/framework/bundle/"
--            }
--        },
--        "Require-Bundle-Instance" : []
--    },
--    "instanceProps" : {}
--}"

-- Edited startup column (UserLayersLayerPlugin inserted):
--  "{
--    "title" : "Map",
--    "fi" : "mapfull",
--    "sv" : "mapfull",
--    "en" : "mapfull",
--    "bundlename" : "mapfull",
--    "bundleinstancename" : "mapfull",
--    "metadata" : {
--        "Import-Bundle" : {
--            "core-base" : {
--                "bundlePath" : "/Oskari/packages/framework/bundle/"
--            },
--            "core-map" : {
--                "bundlePath" : "/Oskari/packages/framework/bundle/"
--            },
-- ...
-- ...   Add start
--             "mapuserlayers" : {
--                "bundlePath" : "/Oskari/packages/framework/bundle/"
--            },
--           "oskariui" : {
--                "bundlePath" : "/Oskari/packages/framework/bundle/"
--            },
--            "mapfull" : {
--                "bundlePath" : "/Oskari/packages/framework/bundle/"
--            },
--            "ui-components": {
--                "bundlePath": "/Oskari/packages/framework/bundle/"
--            }
--        },
--        "Require-Bundle-Instance" : []
--    },
--    "instanceProps" : {}
--}"

--Update mapfull startup in portti_view_bundle_seq in default view

UPDATE portti_view_bundle_seq
   SET startup=[your edited startup] 
 WHERE view_id = (SELECT id FROM portti_view WHERE type = 'DEFAULT') and bundle_id=(SELECT id FROM portti_bundle WHERE name = 'mapfull');
