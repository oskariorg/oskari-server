
INSERT INTO portti_bundle (name, startup) 
       VALUES ('printout','{}');


UPDATE portti_bundle set startup = '{
    "title": "Printout",
    "bundleinstancename": "printout",
    "fi": "printout",
    "sv": "printout",
    "en": "printout",
    "bundlename": "printout",
    "metadata": {
        "Import-Bundle": {
            "printout": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE name = 'printout';
