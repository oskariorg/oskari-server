-- Backend Status;


INSERT INTO portti_bundle (name, startup)
    VALUES ('backendstatus',
'{                    
                    "title": "backendstatus",
                    "bundleinstancename": "backendstatus",
                    "fi": "backendstatus",
                    "sv": "backendstatus",
                    "en": "backendstatus",
                    "bundlename": "backendstatus",
                    "metadata": {
                        "Import-Bundle": {
                            "backendstatus": {
                                "bundlePath": "/Oskari/packages/framework/bundle/"
                            }
                        },
                        "Require-Bundle-Instance": [ ]
                    },
                    "instanceProps": {}
                }
}');

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
    VALUES  (
        (SELECT max(id) FROM portti_view WHERE name='default'), 
        (SELECT max(id) FROM portti_bundle), 
        19,'{}','{}',
        '{
                            "title": "backendstatus",
                            "bundleinstancename": "backendstatus",
                            "fi": "backendstatus",
                            "sv": "backendstatus",
                            "en": "backendstatus",
                            "bundlename": "backendstatus",
                            "metadata": {
                                "Import-Bundle": {
                                    "backendstatus": {
                                        "bundlePath": "/Oskari/packages/framework/bundle/"
                                    }
                                },
                                "Require-Bundle-Instance": [ ]
                            },
                            "instanceProps": {}
                        }
        }');
