
INSERT INTO portti_bundle (name, startup) 
       VALUES ('promote','{}');


UPDATE portti_bundle set startup = '{
        "title" : "Promote",
        "bundleinstancename" : "promote",
        "fi" : "promote",
        "sv" : "promote",
        "en" : "promote",
        "bundlename" : "promote",
        "metadata" : {
            "Import-Bundle" : {
                "promote" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                }
            }
        }
    }' WHERE name = 'promote';
