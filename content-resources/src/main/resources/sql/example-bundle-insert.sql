
-- register bundle '[bundleId]'
INSERT INTO portti_bundle (name, startup)
       VALUES ('[bundleId]','{
    "title": "[optional name for bundle]",
    "bundleinstancename": "[bundleId]",
    "bundlename": "[bundleId]",
    "metadata": {
        "Import-Bundle": {
            "[bundleId]": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        }
    }
}');

-- add bundle '[bundleId]' to view [view_id]
INSERT INTO portti_view_bundle_seq (view_id, seqno, bundle_id, startup, config, state)
       VALUES ([view_id],
        (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [view_id]),
        (SELECT id FROM portti_bundle WHERE name = '[bundleId]'),
        (SELECT startup FROM portti_bundle WHERE name = '[bundleId]'),
        (SELECT config FROM portti_bundle WHERE name = '[bundleId]'),
        (SELECT state FROM portti_bundle WHERE name = '[bundleId]'));
