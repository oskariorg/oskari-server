-- SQL-parser can't run this because it parses the file and splits statements on semicolon character ;
-- Run these manually;
-- Trigger: trigger_user_layer on user_layer;


DROP TRIGGER IF EXISTS trigger_user_layer ON user_layer;
DROP TRIGGER IF EXISTS trigger_user_layer_update ON user_layer_data;

-- Function: procedure_user_layer_update();
CREATE OR REPLACE FUNCTION procedure_user_layer_update()
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

-- Trigger: trigger_user_layer on user_layer;
CREATE TRIGGER trigger_user_layer
BEFORE INSERT OR UPDATE
ON user_layer
FOR EACH ROW
EXECUTE PROCEDURE procedure_user_layer_update();


-- Function: procedure_user_layer_data_update()
CREATE OR REPLACE FUNCTION procedure_user_layer_data_update()
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

-- Trigger: trigger_user_layer_update on user_layer_data
CREATE TRIGGER trigger_user_layer_update
BEFORE INSERT OR UPDATE
ON user_layer_data
FOR EACH ROW
EXECUTE PROCEDURE procedure_user_layer_data_update();
