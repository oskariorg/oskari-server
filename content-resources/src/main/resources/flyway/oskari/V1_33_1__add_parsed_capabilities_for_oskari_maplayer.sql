
-- Add placeholder for parsed capabilities data/layer
ALTER TABLE oskari_maplayer
  ADD COLUMN capabilities TEXT DEFAULT '{}';