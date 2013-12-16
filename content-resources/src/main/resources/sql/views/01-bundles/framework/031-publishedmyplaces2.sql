
INSERT INTO portti_bundle (name, startup) 
       VALUES ('publishedmyplaces2','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Publishedmyplaces2",
    "fi" : "publishedmyplaces2",
    "sv" : "publishedmyplaces2",
    "en" : "publishedmyplaces2",
    "bundlename" : "publishedmyplaces2",
    "bundleinstancename" : "publishedmyplaces2",
    "metadata" : {
        "Import-Bundle" : {
            "publishedmyplaces2" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'publishedmyplaces2';
