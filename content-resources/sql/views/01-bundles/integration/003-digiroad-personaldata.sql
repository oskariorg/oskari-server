
INSERT INTO portti_bundle (name, startup) 
       VALUES ('digiroad-personaldata','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Omat tiedot",
    "fi" : "personaldata",
    "sv" : "?",
    "en" : "?",
    "bundlename" : "digiroad-personaldata",
    "bundleinstancename" : "digiroad-personaldata",
    "metadata" : {
        "Import-Bundle" : {
            "digiroad-personaldata" : {
                "bundlePath" : "../../packages/digiroad/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'digiroad-personaldata';
