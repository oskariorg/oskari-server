ALTER TABLE oskari_maplayer_group
  ADD COLUMN order_number integer DEFAULT null;
  
ALTER TABLE oskari_maplayer_group_link
  ADD COLUMN order_number integer DEFAULT null;