
INSERT INTO portti_bundle (name, startup) 
       VALUES ('publishedstatehandler','{}');

UPDATE portti_bundle set startup = '{
    "title" : "PublishedStatehandler",
    "fi" : "publishedstatehandler",
    "sv" : "publishedstatehandler",
    "en" : "publishedstatehandler",
    "bundlename" : "publishedstatehandler",
    "bundleinstancename" : "publishedstatehandler",
    "metadata" : {
        "Import-Bundle" : {
            "publishedstatehandler" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
             }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'publishedstatehandler';


--------------------------------------------
-- 5. PublishedStatehandler
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='PUBLISH'), 
    (SELECT id FROM portti_bundle WHERE name = 'publishedstatehandler'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='PUBLISH')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "PublishedStatehandler",
        "fi" : "publishedstatehandler",
        "sv" : "publishedstatehandler",
        "en" : "publishedstatehandler",
        "bundlename" : "publishedstatehandler",
        "bundleinstancename" : "publishedstatehandler",
        "metadata" : {
            "Import-Bundle" : {
                "publishedstatehandler" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                 }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'publishedstatehandler') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='PUBLISH');
