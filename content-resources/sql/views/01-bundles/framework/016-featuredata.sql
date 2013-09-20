
INSERT INTO portti_bundle (name, startup) 
       VALUES ('featuredata','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Feature data",
    "fi" : "featuredata",
    "sv" : "featuredata",
    "en" : "featuredata",
    "bundlename" : "featuredata",
    "bundleinstancename" : "featuredata",
    "metadata" : {
        "Import-Bundle" : {
            "featuredata" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'featuredata';
