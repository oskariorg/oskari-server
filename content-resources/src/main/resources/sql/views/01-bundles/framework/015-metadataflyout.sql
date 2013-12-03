
INSERT INTO portti_bundle (name, startup) 
       VALUES ('metadataflyout','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Metadata Flyout",
    "fi" : "metadataflyout",
    "sv" : "metadataflyout",
    "en" : "metadataflyout",
    "bundlename" : "metadataflyout",
    "bundleinstancename" : "metadataflyout",
    "metadata" : {
        "Import-Bundle" : {
            "metadataflyout" : {
                "bundlePath" : "/Oskari/packages/catalogue/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'metadataflyout';
