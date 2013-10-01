
INSERT INTO portti_bundle (name, startup) 
       VALUES ('digiroad-featureselector','{}');

UPDATE portti_bundle set startup = '{
    "title" : "My places",
    "fi" : "Kohteet",
    "sv" : "Platsar",
    "en" : "Places",
    "bundlename" : "digiroad-featureselector",
    "bundleinstancename" : "digiroad-featureselector",
    "metadata" : {
        "Import-Bundle" : {
            "digiroad-featureselector" : {
                "bundlePath" : "../../packages/digiroad/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'digiroad-featureselector';
