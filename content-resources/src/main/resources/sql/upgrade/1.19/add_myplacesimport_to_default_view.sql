INSERT INTO portti_view_bundle_seq(
    view_id, bundle_id, seqno, config, startup, bundleinstance
) VALUES(
    (SELECT id FROM portti_view WHERE type = 'DEFAULT'),
    (SELECT id FROM portti_bundle WHERE name = 'myplacesimport'),
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')),
    (SELECT config FROM portti_bundle WHERE name = 'myplacesimport'),
    (SELECT startup FROM portti_bundle WHERE name = 'myplacesimport'),
    'myplacesimport'
);