-- update startup to point to new implementation
UPDATE portti_bundle SET startup='{
    "bundlename" : "statsgrid",
    "metadata" : {
        "Import-Bundle" : {
            "statsgrid" : {
                "bundlePath" : "/Oskari/packages/statistics/"
            }
        }
    }
}' where name = 'statsgrid';
