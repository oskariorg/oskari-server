
INSERT INTO portti_bundle (name, startup) 
       VALUES ('featuredata2','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Feature data",
    "fi" : "featuredata2",
    "sv" : "featuredata2",
    "en" : "featuredata2",
    "bundlename" : "featuredata2",
    "bundleinstancename" : "featuredata2",
    "metadata" : {
        "Import-Bundle" : {
            "featuredata2" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'featuredata2';

--UPDATE portti_bundle set config = '{
--    "selectionTools": true
--}' WHERE name = 'featuredata2';