
INSERT INTO portti_bundle (name, startup) 
       VALUES ('search','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Search",
    "fi" : "search",
    "sv" : "?",
    "en" : "?",
    "bundlename" : "search",
    "bundleinstancename" : "search",
    "metadata" : {
        "Import-Bundle" : {
            "search" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'search';
