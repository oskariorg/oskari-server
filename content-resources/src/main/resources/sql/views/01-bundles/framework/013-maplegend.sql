
INSERT INTO portti_bundle (name, startup) 
       VALUES ('maplegend','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Map legend",
    "fi" : "maplegend",
    "sv" : "maplegend",
    "en" : "maplegend",
    "bundlename" : "maplegend",
    "bundleinstancename" : "maplegend",
    "metadata" : {
        "Import-Bundle" : {
            "maplegend" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'maplegend';
