
INSERT INTO portti_bundle (name, startup) 
       VALUES ('publisher','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Map publisher",
    "fi" : "publisher",
    "sv" : "publisher",
    "en" : "publisher",
    "bundlename" : "publisher",
    "bundleinstancename" : "publisher",
    "metadata" : {
        "Import-Bundle" : {
            "publisher" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'publisher';
