DROP TRIGGER IF EXISTS trigger_appsetup_update ON oskari_appsetup;

CREATE OR REPLACE FUNCTION procedure_appsetup_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF (TG_OP = 'UPDATE') THEN
		NEW.updated := current_timestamp;
	    RETURN NEW;
	END IF;
	RETURN NEW;
END;
$$;

CREATE TRIGGER trigger_appsetup_update BEFORE INSERT OR UPDATE ON oskari_appsetup FOR EACH ROW EXECUTE PROCEDURE procedure_appsetup_update();