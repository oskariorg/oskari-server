
INSERT INTO portti_bundle (name, startup) 
       VALUES ('metadatacatalogue','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Metadata Catalogue",
    "fi" : "metadatacatalogue",
    "sv" : "metadatacatalogue",
    "en" : "metadatacatalogue",
    "bundlename" : "metadatacatalogue",
    "bundleinstancename" : "metadatacatalogue",
    "metadata" : {
        "Import-Bundle" : {
            "metadatacatalogue" : {
                "bundlePath" : "/Oskari/packages/catalogue/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'metadatacatalogue';
