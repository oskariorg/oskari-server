
INSERT INTO portti_bundle (name, startup) 
       VALUES ('statehandler','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Statehandler",
    "fi" : "statehandler",
    "sv" : "statehandler",
    "en" : "statehandler",
    "bundlename" : "statehandler",
    "bundleinstancename" : "statehandler",
    "metadata" : {
        "Import-Bundle" : {
            "statehandler" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'statehandler';
