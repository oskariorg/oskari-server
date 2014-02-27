-- add a column to indicate whether the layer is a real time layer;
ALTER TABLE oskari_maplayer ADD COLUMN realtime boolean DEFAULT false;
-- the layer's refresh rate in seconds
ALTER TABLE oskari_maplayer ADD COLUMN refresh_rate integer DEFAULT 0;