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
            "mapstats" : {
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
            },
            "ui-components": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }

        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {
}}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') 
    AND view_id=(SELECT id FROM portti_view WHERE type='PUBLISH');


UPDATE portti_view_bundle_seq set config = '{
    "globalMapAjaxUrl": "[REPLACED BY HANDLER]",
    "imageLocation": "/Oskari/resources",
    "plugins" : [
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.WmsLayerPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin", 
         "config" : { 
           "contextPath" : "/transport-0.0.1", 
           "hostname" : "localhost", 
           "port" : "8888",
           "lazy" : true,
           "disconnectTime" : 30000,
           "backoffIncrement": 1000,
           "maxBackoff": 60000,
           "maxNetworkDelay": 10000
         }
       },
       { "id" : "Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.LogoPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapstats.plugin.StatsLayerPlugin" }
      ],
      "layers": [
      ]
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') 
    AND view_id=(SELECT id FROM portti_view WHERE type='PUBLISH');

    
UPDATE portti_view_bundle_seq SET startup='{
        "title": "Map",
        "fi": "Map",
        "sv": "?",
        "en": "Map",
        "bundlename": "mapfull",
        "bundleinstancename": "mapfull",
        "metadata": {
            "Import-Bundle": {
                "mapwmts": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "mapwfs2": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "mapstats": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "service-base": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "event-map-layer": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "request-map-layer": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "mapmodule-plugin": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "event-base": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "mapfull": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "core-base": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "oskariui": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "request-base": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "domain": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "core-map": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "request-map": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "sandbox-base": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "service-map": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "sandbox-map": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                },
                "event-map": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                }
            },
            "Require-Bundle-Instance": []
        },
        "instanceProps": {}
}'
WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') AND
view_id IN (SELECT id FROM portti_view WHERE type='PUBLISHED');