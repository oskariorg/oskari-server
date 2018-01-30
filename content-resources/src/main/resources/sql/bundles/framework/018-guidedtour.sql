
INSERT INTO portti_bundle (name, startup) 
       VALUES ('guidedtour','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Guided Tour",
    "fi" : "guidedtour",
    "sv" : "guidedtour",
    "en" : "guidedtour",
    "bundlename" : "guidedtour",
    "bundleinstancename" : "guidedtour",
    "metadata" : {
        "Import-Bundle" : {
            "guidedtour" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'guidedtour';
