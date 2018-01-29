
INSERT INTO portti_bundle (name, startup) 
       VALUES ('statsgrid','{}');


UPDATE portti_bundle set startup = '{
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
}' WHERE name = 'statsgrid';

UPDATE portti_bundle set config = '{
    "name": "StatsGrid",
    "sandbox": "sandbox",
    "stateful" : true,
    "tileClazz": "Oskari.statistics.bundle.statsgrid.Tile",
    "viewClazz": "Oskari.statistics.bundle.statsgrid.StatsView"
}' WHERE name = 'statsgrid';