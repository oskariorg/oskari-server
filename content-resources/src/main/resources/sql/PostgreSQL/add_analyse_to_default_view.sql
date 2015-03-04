-- 
--
-- add analyse bundle to bundle table, if not there

INSERT INTO portti_bundle (name, startup) 
       VALUES ('analyse','{}');


UPDATE portti_bundle set startup = '{
    "title": "Analysis UI",
    "bundleinstancename": "analyse",
    "fi": "analyysi",
    "sv": "analys",
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
}' WHERE name = 'analyse';


-- Add analyse bundle  to default view

INSERT INTO portti_view_bundle_seq(
    view_id, bundle_id, seqno, config, startup, bundleinstance
) VALUES(
    (SELECT id FROM portti_view WHERE type = 'DEFAULT'),
    (SELECT id FROM portti_bundle WHERE name = 'analyse'),
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
    (SELECT config FROM portti_bundle WHERE name = 'analyse'),
    (SELECT startup FROM portti_bundle WHERE name = 'analyse'),
    'analyse'
);

-- STOP HERE and Try, if it works--
--
-- If you have very old version, you should goon following steps
-- Add mapanalysis LayerPlugin to default mapfull config
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Select first your current mapfull config !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
SELECT view_id, config
  FROM portti_view_bundle_seq where view_id = (SELECT id FROM portti_view WHERE type = 'DEFAULT') and bundle_id=(SELECT id FROM portti_bundle WHERE name = 'mapfull');

-- Update mapfull config 
-- Add to config json string the  UserlayersLayerPlugin section
-- e.g.
-- SELECT result :
-- {"globalMapAjaxUrl":"[REPLACED BY HANDLER]","plugins":[{"id":"Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin"},{"id":"Oskari.mapframework.mapmodule.WmsLayerPlugin"},{"id":"Oskari.mapframework.mapmodule.MarkersPlugin"},{"id":"Oskari.mapframework.mapmodule.ControlsPlugin"},{"id":"Oskari.mapframework.mapmodule.GetInfoPlugin"},{"id":"Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin"},{"id":"Oskari.mapframework.bundle.mapmodule.plugin.ScaleBarPlugin"},{"id":"Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar"},{"id":"Oskari.mapframework.bundle.mapmodule.plugin.PanButtons"},{"id":"Oskari.mapframework.bundle.mapmodule.plugin.FullScreenPlugin"}],"layers":[{"id":1}],"imageLocation":"/Oskari/resources"}

-- Edited config column (mapanalysis LayerPlugin inserted):
-- - your config cound be diffrent , just add mapanalysis plugin
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
-- 	}, {                                                                            <--------
--      "id": "Oskari.mapframework.bundle.mapanalysis.plugin.AnalysisLayerPlugin"   <--------  add this
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

 -- Add mapanalysis LayerPlugin to default mapfull startup
-- Select first your current mapfull startup
SELECT view_id, startup
  FROM portti_view_bundle_seq where view_id = (SELECT id FROM portti_view WHERE type = 'DEFAULT') and bundle_id=(SELECT id FROM portti_bundle WHERE name = 'mapfull');

-- Update mapfull startup
-- Add to startup json string the  mapanalysis section
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
-- ...
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

-- EDITED startup column (mapanalysis LayerPlugin inserted):
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
-- ...
-- ...
-- ...   Add 3 rows
--              "mapanalysis" : {                                         <--------
--                "bundlePath" : "/Oskari/packages/framework/bundle/"     <--------
--            },                                                          <--------
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
