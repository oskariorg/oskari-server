
INSERT INTO portti_bundle (name, startup) 
       VALUES ('parcelselector','{}');


UPDATE portti_bundle set startup = '{
        "title" : "Parcel Selector",
        "bundleinstancename" : "parcelselector",
        "fi" : "Määräalan valinta",
        "sv" : "parcelselector",
        "en" : "parcelselector",
        "bundlename" : "parcelselector",
        "metadata" : {
            "Import-Bundle" : {
                "parcelselector" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                }
            }
        }
    }' WHERE name = 'parcelselector';
