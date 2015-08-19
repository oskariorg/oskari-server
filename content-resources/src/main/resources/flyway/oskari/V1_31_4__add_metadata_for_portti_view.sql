-- Add metadata column to portti_view
ALTER TABLE portti_view
  ADD COLUMN metadata TEXT DEFAULT '{}'::TEXT;
