
INSERT INTO portti_bundle (name, startup) 
       VALUES ('publishedgrid','{}');


UPDATE portti_bundle set startup = '{
    "title": "Published Grid",
    "bundleinstancename": "publishedgrid",
    "fi": "publishedgrid",
    "sv": "publishedgrid",
    "en": "publishedgrid",
    "bundlename": "publishedgrid",
    "metadata": {
        "Import-Bundle": {
            "publishedgrid": {
                "bundlePath": "/Oskari/packages/statistics/bundle/"
            },
            "geostats": {
                "bundlePath": "/Oskari/packages/libraries/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE name = 'publishedgrid';
