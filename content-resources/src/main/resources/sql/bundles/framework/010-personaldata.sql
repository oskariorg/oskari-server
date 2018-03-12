
INSERT INTO portti_bundle (name, startup) 
       VALUES ('personaldata','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Personal data",
    "fi" : "personaldata",
    "sv" : "personaldata",
    "en" : "personaldata",
    "bundlename" : "personaldata",
    "bundleinstancename" : "personaldata",
    "metadata" : {
        "Import-Bundle" : {
            "personaldata" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
             }
         },
         "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'personaldata';
