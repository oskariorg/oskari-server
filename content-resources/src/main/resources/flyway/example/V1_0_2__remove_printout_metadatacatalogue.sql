DELETE FROM portti_view_bundle_seq WHERE EXISTS
(SELECT id FROM portti_bundle WHERE portti_bundle.id = portti_view_bundle_seq.bundle_id AND name = 'printout');

