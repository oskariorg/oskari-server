ALTER TABLE oskari_announcements ALTER COLUMN begin_date TYPE timestamptz USING begin_date :: timestamptz;
ALTER TABLE oskari_announcements ALTER COLUMN end_date TYPE timestamptz USING end_date :: timestamptz + interval '23 hours 59 minutes';
ALTER TABLE oskari_announcements ADD COLUMN options text DEFAULT '{}'::text;
UPDATE oskari_announcements SET options = format('{"showAsPopup": %s}',to_json(active));
ALTER TABLE oskari_announcements DROP COLUMN active;
