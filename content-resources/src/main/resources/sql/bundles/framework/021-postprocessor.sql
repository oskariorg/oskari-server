
-- postprocessor bundle is not currently linked to any view but inserted with code;
-- if a defined parameter is given, so this sql is a bit different from the usual bundle sqls;

INSERT INTO portti_bundle (name, startup) 
       VALUES ('postprocessor','{}');


UPDATE portti_bundle set startup = '{
    "title": "Post processor",
    "bundleinstancename": "postprocessor",
    "fi": "postprocessor",
    "sv": "postprocessor",
    "en": "postprocessor",
    "bundlename": "postprocessor",
    "metadata": {
        "Import-Bundle": {
            "postprocessor": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE name = 'postprocessor';
