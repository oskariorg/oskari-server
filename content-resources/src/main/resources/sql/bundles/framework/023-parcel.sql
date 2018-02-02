
INSERT INTO portti_bundle (name, startup) 
       VALUES ('parcel','{}');


UPDATE portti_bundle set startup = '{
        "title" : "Parcel",
        "bundleinstancename" : "parcel",
        "fi" : "Määräala",
        "sv" : "parcel",
        "en" : "parcel",
        "bundlename" : "parcel",
        "metadata" : {
            "Import-Bundle" : {
                "parcel" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                }
            }
        }
    }' WHERE name = 'parcel';
