-- add supplement table columns to view

ALTER TABLE portti_view
   ADD  only_uuid boolean DEFAULT FALSE,
   ADD  creator bigint DEFAULT (-1),
   ADD  domain character varying(512) DEFAULT ''::character varying,
   ADD  lang character varying(2) DEFAULT 'en'::character varying,
   ADD  is_public boolean DEFAULT FALSE,
   ADD  old_id bigint DEFAULT (-1),
   ADD  created timestamp DEFAULT CURRENT_TIMESTAMP;

-- ALTER TABLE portti_view RENAME TO oskari_view;

-- copy field values from supplement;
UPDATE portti_view AS a
SET
  creator=b.creator,
  domain=b.pubdomain,
  lang=b.lang,
  is_public=b.is_public,
  old_id=b.old_id
FROM portti_view_supplement As b
WHERE b.id = a.supplement_id;

-- clean up;
ALTER TABLE portti_view DROP CONSTRAINT portti_view_supplement_id_fkey;
DROP TABLE portti_view_supplement;
ALTER TABLE portti_view DROP COLUMN supplement_id;