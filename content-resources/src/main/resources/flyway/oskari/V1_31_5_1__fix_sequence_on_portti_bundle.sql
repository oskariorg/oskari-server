-- Updates have inserted rows including id on the insert, this breaks the internal sequencing. This is the fix:
SELECT setval('portti_bundle_id_seq', (SELECT MAX(id) FROM portti_bundle));