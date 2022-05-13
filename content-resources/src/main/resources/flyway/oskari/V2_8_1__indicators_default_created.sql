-- Set default after column is created to keep value empty for existing indicators.

ALTER TABLE oskari_statistical_indicator
ALTER COLUMN "created" SET DEFAULT current_timestamp;