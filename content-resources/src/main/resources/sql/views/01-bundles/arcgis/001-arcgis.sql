
INSERT INTO portti_bundle (name, startup) 
       VALUES ('arcgis','{}');


UPDATE portti_bundle set startup = '{
    "title": "arcgis UI",
    "bundleinstancename": "maparcgis",
    "fi": "maparcgis",
    "sv": "maparcgis",
    "en": "maparcgis",
    "bundlename": "maparcgis",
    "metadata": {
        "Import-Bundle": {
            "maparcgis": {
                "bundlePath": "/Oskari/packages/arcgis/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE name = 'arcgis';
