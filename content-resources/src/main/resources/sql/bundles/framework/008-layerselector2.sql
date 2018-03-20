
INSERT INTO portti_bundle (name, startup) 
       VALUES ('layerselector2','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Maplayer selection",
    "fi" : "layerselector2",
    "sv" : "layerselector2",
    "en" : "layerselector2",
    "bundlename" : "layerselector2",
    "bundleinstancename" : "layerselector2",
    "metadata" : {
        "Import-Bundle" : {
            "layerselector2" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'layerselector2';
