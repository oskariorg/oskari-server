-- previously varchar 20000, not enough for some layers;
ALTER table portti_capabilities_cache  ALTER COLUMN data TYPE text;