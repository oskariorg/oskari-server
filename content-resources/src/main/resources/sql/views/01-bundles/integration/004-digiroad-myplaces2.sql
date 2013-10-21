
INSERT INTO portti_bundle (name, startup) 
       VALUES ('digiroad-myplaces2','{}');

UPDATE portti_bundle set startup = '{
    "title" : "My places",
    "fi" : "Kohteet",
    "sv" : "Platsar",
    "en" : "Places",
    "bundlename" : "digiroad-myplaces2",
    "bundleinstancename" : "digiroad-myplaces2",
    "metadata" : {
        "Import-Bundle" : {
            "digiroad-myplaces2" : {
                "bundlePath" : "../../packages/digiroad/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'digiroad-myplaces2';
