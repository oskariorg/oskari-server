UPDATE oskari_maplayer_group
    SET order_number = 1000000;

UPDATE oskari_maplayer_group_link
SET order_number = 1000000;

ALTER TABLE oskari_maplayer_group_link
  ALTER COLUMN order_number SET DEFAULT 1000000;

ALTER TABLE oskari_maplayer_group
  ALTER COLUMN order_number SET DEFAULT 1000000;