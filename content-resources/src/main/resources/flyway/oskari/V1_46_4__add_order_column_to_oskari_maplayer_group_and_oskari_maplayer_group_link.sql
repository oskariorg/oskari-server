ALTER TABLE oskari_maplayer_group
  ADD COLUMN order integer DEFAULT null;
  
ALTER TABLE oskari_maplayer_group_link
  ADD COLUMN order integer DEFAULT null;