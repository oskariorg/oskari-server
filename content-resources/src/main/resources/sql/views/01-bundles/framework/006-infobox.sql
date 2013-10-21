
INSERT INTO portti_bundle (name, startup) 
       VALUES ('infobox','{}');

UPDATE portti_bundle set startup = '{
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
}' WHERE name = 'infobox';

UPDATE portti_bundle set config = '{
  "adaptable": true
}' WHERE name = 'infobox';
