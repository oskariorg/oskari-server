-- add guided tour bundle
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, bundleinstance)
(
	SELECT
		view_id,
		(SELECT id FROM portti_bundle WHERE name = 'guidedtour') AS bundle_id,
		max(seqno) + 1 AS seqno,
		'{}' AS config,
		'{}' AS state,
		'guidedtour' AS bundleinstance
	FROM portti_view_bundle_seq WHERE EXISTS
		(SELECT id FROM portti_view WHERE portti_view.id = portti_view_bundle_seq.view_id AND type IN ('USER', 'DEFAULT'))
	GROUP BY view_id
);

-- add new bundle with content shown in guided tour
INSERT INTO portti_bundle (name) VALUES ('sample-info');

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, bundleinstance)
(
	SELECT
		view_id,
		(SELECT id FROM portti_bundle WHERE name = 'sample-info') AS bundle_id,
		max(seqno) + 1 AS seqno,
		'{}' AS config,
		'{}' AS state,
		'sample-info' AS bundleinstance
	FROM portti_view_bundle_seq WHERE EXISTS
		(SELECT id FROM portti_view WHERE portti_view.id = portti_view_bundle_seq.view_id AND type IN ('USER', 'DEFAULT'))
	GROUP BY view_id
);

