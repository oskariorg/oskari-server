-- SQL-parser can't run this because it parses the file and splits statements on semicolon character ;
-- Run these manually;
-- Function: procedure_preparcel_update()

CREATE OR REPLACE FUNCTION procedure_preparcel_update()
  RETURNS trigger AS
$BODY$
BEGIN
	IF (TG_OP = 'UPDATE') THEN
		NEW.updated := current_timestamp;
	RETURN NEW;
	ELSIF (TG_OP = 'INSERT') THEN
		NEW.created := current_timestamp;
	RETURN NEW;
	END IF;
	RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


-- Trigger: trigger_preparcel on preparcel

DROP TRIGGER IF EXISTS trigger_preparcel ON preparcel;

CREATE TRIGGER trigger_preparcel
  BEFORE INSERT OR UPDATE
  ON preparcel
  FOR EACH ROW
  EXECUTE PROCEDURE procedure_preparcel_update();

-- Function: procedure_preparcel_data_update()

CREATE OR REPLACE FUNCTION procedure_preparcel_data_update()
  RETURNS trigger AS
$BODY$
BEGIN
	IF (TG_OP = 'UPDATE') THEN
		NEW.updated := current_timestamp;
	RETURN NEW;
	ELSIF (TG_OP = 'INSERT') THEN
		NEW.created := current_timestamp;
	RETURN NEW;
	END IF;
	RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- Trigger: trigger_preparcel_update on preparcel_data

DROP TRIGGER IF EXISTS trigger_preparcel_update ON preparcel_data;

CREATE TRIGGER trigger_preparcel_update
  BEFORE INSERT OR UPDATE
  ON preparcel_data
  FOR EACH ROW
  EXECUTE PROCEDURE procedure_preparcel_data_update();
