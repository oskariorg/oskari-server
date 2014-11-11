
-- register bundle
INSERT INTO portti_bundle (name, startup)
  VALUES ('rpc','{}');

UPDATE portti_bundle set startup = '{
    "title": "Remote procedure call",
    "bundleinstancename": "rpc",
    "bundlename": "rpc",
    "metadata": {
        "Import-Bundle": {
            "rpc": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        }
    }
}' WHERE name = 'rpc';

-- add bundle to publish template
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup)
  VALUES ((SELECT id FROM portti_view WHERE type='PUBLISH'),
          (SELECT id FROM portti_bundle WHERE name = 'rpc'),
          (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='PUBLISH')),
          '{}','{}', '{}');

-- update proper startup for bundle
UPDATE portti_view_bundle_seq set startup = '{
    "title": "Remote procedure call",
    "bundleinstancename": "rpc",
    "bundlename": "rpc",
    "metadata": {
        "Import-Bundle": {
            "rpc": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        }
    }
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'rpc')
             AND  view_id=(SELECT id FROM portti_view WHERE type='PUBLISH');