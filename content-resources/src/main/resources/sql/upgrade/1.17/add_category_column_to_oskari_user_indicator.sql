ALTER TABLE oskari_user_indicator ADD COLUMN category CHARACTER VARYING(100);

-- update every user indicator to be of category 'KUNTA'
UPDATE oskari_user_indicator SET category = 'KUNTA';