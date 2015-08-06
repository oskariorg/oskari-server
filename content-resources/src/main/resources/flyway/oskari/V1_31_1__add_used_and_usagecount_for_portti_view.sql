-- Add used column to portti_view
ALTER TABLE portti_view
  ADD COLUMN used timestamp without time zone NOT NULL DEFAULT now();

-- Add usagecount column to portti_view
ALTER TABLE portti_view
  ADD COLUMN usagecount bigint NOT NULL DEFAULT 0;