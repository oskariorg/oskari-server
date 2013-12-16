INSERT INTO portti_bundle (name, startup) 
       VALUES ('publishedmyplaces2','{
    "title" : "Publishedmyplaces2",
    "fi" : "publishedmyplaces2",
    "sv" : "publishedmyplaces2",
    "en" : "publishedmyplaces2",
    "bundlename" : "publishedmyplaces2",
    "bundleinstancename" : "publishedmyplaces2",
    "metadata" : {
        "Import-Bundle" : {
            "publishedmyplaces2" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}');

--------------------------------------------
-- 6. Publishedmyplaces2
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='PUBLISH'), 
    (SELECT id FROM portti_bundle WHERE name = 'publishedmyplaces2'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='PUBLISH')), 
    '{}','{}', '{
            "title" : "Publishedmyplaces2",
            "fi" : "publishedmyplaces2",
            "sv" : "publishedmyplaces2",
            "en" : "publishedmyplaces2",
            "bundlename" : "publishedmyplaces2",
            "bundleinstancename" : "publishedmyplaces2",
            "metadata" : {
                "Import-Bundle" : {
                    "publishedmyplaces2" : {
                        "bundlePath" : "/Oskari/packages/framework/bundle/"
                    }
                },
                "Require-Bundle-Instance" : []
            },
            "instanceProps" : {}
        }');