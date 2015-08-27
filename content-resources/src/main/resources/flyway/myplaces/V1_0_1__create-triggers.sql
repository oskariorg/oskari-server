-- SQL-parser can't run this because it parses the file and splits statements on semicolon character ;
-- Run these manually;
-- Trigger: trigger_my_places_update on my_places;

CREATE OR REPLACE FUNCTION procedure_my_places_update()
  RETURNS TRIGGER AS
  $BODY$
  BEGIN
    IF (TG_OP = 'UPDATE')
    THEN
      NEW.updated := current_timestamp;
      RETURN NEW;
    ELSIF (TG_OP = 'INSERT')
      THEN
        NEW.created := current_timestamp;
        RETURN NEW;
    END IF;
    RETURN NEW;
  END;
  $BODY$
LANGUAGE plpgsql VOLATILE
COST 100;

DROP TRIGGER IF EXISTS trigger_my_places_update ON my_places;

CREATE TRIGGER trigger_my_places_update
BEFORE INSERT OR UPDATE
ON my_places
FOR EACH ROW
EXECUTE PROCEDURE procedure_my_places_update();
