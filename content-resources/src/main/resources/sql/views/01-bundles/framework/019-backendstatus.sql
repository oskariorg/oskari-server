
INSERT INTO portti_bundle (name, startup) 
       VALUES ('backendstatus','{}');


UPDATE portti_bundle set startup = '{
    "title": "Backend status",
    "bundleinstancename": "backendstatus",
    "fi": "backendstatus",
    "sv": "backendstatus",
    "en": "backendstatus",
    "bundlename": "backendstatus",
    "metadata": {
        "Import-Bundle": {
            "backendstatus": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE name = 'backendstatus';
