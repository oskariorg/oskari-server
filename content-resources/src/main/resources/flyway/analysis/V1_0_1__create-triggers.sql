--
-- PostgreSQL database
-- triggers for analysis tables
--

DROP TRIGGER IF EXISTS trigger_analysis_update ON analysis_data;
DROP TRIGGER IF EXISTS trigger_analysis ON analysis;

CREATE OR REPLACE FUNCTION procedure_analysis_data_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;

CREATE OR REPLACE FUNCTION procedure_analysis_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;

CREATE TRIGGER trigger_analysis BEFORE INSERT OR UPDATE ON analysis FOR EACH ROW EXECUTE PROCEDURE procedure_analysis_update();
CREATE TRIGGER trigger_analysis_update BEFORE INSERT OR UPDATE ON analysis_data FOR EACH ROW EXECUTE PROCEDURE procedure_analysis_data_update();




