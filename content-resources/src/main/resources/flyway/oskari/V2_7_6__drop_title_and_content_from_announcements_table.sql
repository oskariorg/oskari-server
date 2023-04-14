ALTER TABLE oskari_announcements 
DROP COLUMN title, 
DROP COLUMN content;

ALTER TABLE oskari_announcements
ALTER COLUMN "locale" SET NOT NULL;