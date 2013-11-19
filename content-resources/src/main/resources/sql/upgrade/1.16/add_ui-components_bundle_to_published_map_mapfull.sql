-- update all published maps' mapfull startup to include 'ui-components' bundle;

UPDATE portti_view_bundle_seq set startup = '{
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
                "mapwfs": {
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
                },
                "ui-components": {
                    "bundlePath": "/Oskari/packages/framework/bundle/"
                }
            },
            "Require-Bundle-Instance": []
        },
        "instanceProps": {}
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull')
AND view_id IN (SELECT id FROM portti_view WHERE type='PUBLISHED' OR type='PUBLISH');