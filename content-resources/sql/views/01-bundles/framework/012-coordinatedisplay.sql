
INSERT INTO portti_bundle (name, startup) 
       VALUES ('coordinatedisplay','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Coordinate display",
    "fi" : "coordinatedisplay",
    "sv" : "coordinatedisplay",
    "en" : "coordinatedisplay",
    "bundlename" : "coordinatedisplay",
    "bundleinstancename" : "coordinatedisplay",
    "metadata" : {
        "Import-Bundle" : {
            "coordinatedisplay" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'coordinatedisplay';
