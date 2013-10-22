
INSERT INTO portti_bundle (name, startup) 
       VALUES ('divmanazer','{}');


UPDATE portti_bundle set startup = '{
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
}' WHERE name = 'divmanazer';
