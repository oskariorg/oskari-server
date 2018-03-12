
INSERT INTO portti_bundle (name, startup) 
       VALUES ('publishedstatehandler','{}');

UPDATE portti_bundle set startup = '{
    "title" : "PublishedStatehandler",
    "fi" : "publishedstatehandler",
    "sv" : "publishedstatehandler",
    "en" : "publishedstatehandler",
    "bundlename" : "publishedstatehandler",
    "bundleinstancename" : "publishedstatehandler",
    "metadata" : {
        "Import-Bundle" : {
            "publishedstatehandler" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
             }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'publishedstatehandler';
