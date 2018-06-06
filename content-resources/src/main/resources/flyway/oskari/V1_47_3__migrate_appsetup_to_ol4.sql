
-- to allow incrementing of seqno (temporary unique violation)
ALTER TABLE ONLY portti_view_bundle_seq DROP CONSTRAINT view_seq;

-- add drawtools bundle after mapfull to all views that have type DEFAULT or USER
WITH target_views AS (
	SELECT DISTINCT ON (view_id) view_id, seqno AS mapfull_seq FROM portti_view_bundle_seq, portti_view WHERE
	portti_view.id = view_id AND -- join
	bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') AND -- is mapfull bundle row
	type IN ('DEFAULT', 'USER') AND -- is right view type
	view_id NOT IN (SELECT view_id FROM portti_view_bundle_seq WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'drawtools')) -- view does not already have drawtools
), increment_seqnos AS (
	UPDATE portti_view_bundle_seq AS s
	SET seqno = (seqno + 1)
	FROM target_views m
	WHERE m.view_id = s.view_id AND s.seqno > mapfull_seq
)
INSERT INTO portti_view_bundle_seq(view_id, bundle_id, seqno, startup, bundleinstance) ( -- add drawtools to views: in sequence after mapfull
    SELECT
        view_id,
        (SELECT id FROM portti_bundle WHERE name = 'drawtools'),
        mapfull_seq + 1,
        '{"metadata":{"Import-Bundle":{"drawtools":{"bundlePath":"/Oskari/packages/mapping/ol3/"}}},"bundlename":"drawtools"}',
        'drawtools'
    FROM target_views
);

-- add constraint back
ALTER TABLE ONLY portti_view_bundle_seq ADD CONSTRAINT view_seq UNIQUE (view_id, seqno);


-- delete Open Layers 2 bundle (OL4 is part of mapfull now)
DELETE FROM portti_view_bundle_seq WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'openlayers-default-theme');


-- update bundle startup
WITH mod_list AS (
	SELECT id, t.startup FROM (VALUES
(
'mapfull', '{
		"metadata": {
			"Import-Bundle": {
				"mapmyplaces": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"maparcgis": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"mapmodule": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"oskariui": {
					"bundlePath": "/Oskari/packages/framework/bundle/"
				},
				"mapwfs2": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"mapstats": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"mapuserlayers": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"ui-components": {
					"bundlePath": "/Oskari/packages/framework/bundle/"
				},
				"mapanalysis": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"mapfull": {
					"bundlePath": "/Oskari/packages/framework/bundle/"
				},
				"mapwmts": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				}
			}
		},
		"bundlename": "mapfull"
	}'
),(
'toolbar', '{
  "bundlename": "toolbar",
  "metadata": {
    "Import-Bundle": {
      "toolbar": {
        "bundlePath": "/Oskari/packages/mapping/ol3/"
      }
    }
  }
}'
),(
'infobox', '{
  "bundlename": "infobox",
  "metadata": {
    "Import-Bundle": {
      "infobox": {
        "bundlePath": "/Oskari/packages/mapping/ol3/"
      }
    }
  }
 }'
),(
'analyse', '{
		"metadata": {
			"Import-Bundle": {
				"analyse": {
					"bundlePath": "/Oskari/packages/analysis/ol3/"
				}
			}
		},
		"bundlename": "analyse"
	}'
),(
'featuredata2', '{
		"metadata": {
			"Import-Bundle": {
				"featuredata2": {
					"bundlePath": "/Oskari/packages/framework/"
				}
			}
		},
		"bundlename": "featuredata2"
	}'
),(
'metadatacatalogue', '{
		"metadata": {
			"Import-Bundle": {
				"metadatacatalogue": {
					"bundlePath": "/Oskari/packages/catalogue/"
				}
			}
		},
		"bundlename": "metadatacatalogue"
	}'
),(
'heatmap', '{
    "bundlename" : "heatmap",
    "metadata" : {
        "Import-Bundle" : {
            "heatmap" : {
                "bundlePath" : "/Oskari/packages/mapping/ol3/"
            }
        }
    }
}'
)) AS t (name, startup), portti_bundle WHERE t.name = portti_bundle.name
)
UPDATE portti_view_bundle_seq SET startup = (SELECT startup FROM mod_list WHERE mod_list.id = bundle_id)
FROM portti_view
WHERE
    portti_view.id = portti_view_bundle_seq.view_id AND
    type IN ('DEFAULT', 'USER') AND
    bundle_id IN (SELECT id FROM mod_list);

-- change myplaces2 -> myplaces3
UPDATE portti_view_bundle_seq SET startup = '{
    "bundlename" : "myplaces3",
    "metadata" : {
       "Import-Bundle" : {
          "myplaces3" : {
            "bundlePath" : "/Oskari/packages/framework/bundle/"
          }
       }
    }
  }', config='{}', state='{}', bundle_id = (select id from portti_bundle where name = 'myplaces3')
  where bundle_id = (select id from portti_bundle where name = 'myplaces2');