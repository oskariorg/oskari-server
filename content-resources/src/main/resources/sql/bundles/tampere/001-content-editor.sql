INSERT INTO portti_bundle (name, startup) 
       VALUES ('content-editor','{}');


UPDATE portti_bundle set startup = '{
        "title" : "content-editor",
        "bundleinstancename" : "content-editor",
        "fi" : "content-editor",
        "sv" : "content-editor",
        "en" : "content-editor",
        "bundlename" : "content-editor",
        "metadata" : {
            "Import-Bundle" : {
                "content-editor" : {
                    "bundlePath" : "/Oskari/packages/tampere/bundle/"
                }
            }
        }
    }' WHERE name = 'content-editor';