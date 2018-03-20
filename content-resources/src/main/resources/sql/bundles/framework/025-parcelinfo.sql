
INSERT INTO portti_bundle (name, startup) 
       VALUES ('parcelinfo','{}');


UPDATE portti_bundle set startup = '{
        "title" : "Parcel Info",
        "bundleinstancename" : "parcelinfo",
        "fi" : "Paikan info",
        "sv" : "parcelinfo",
        "en" : "parcelinfo",
        "bundlename" : "parcelinfo",
        "metadata" : {
            "Import-Bundle" : {
                "parcelinfo" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                }
            }
        }
    }' WHERE name = 'parcelinfo';
