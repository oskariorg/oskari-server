--
-- PostgreSQL database 
-- triggers for analysis tables
--

-- Dumped from database version 9.3.1
-- Dumped by pg_dump version 9.3.0
-- Started on 2014-07-04 17:11:47


--
-- TOC entry 1257 (class 1255 OID 116483)
-- Name: procedure_analysis_data_update(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION procedure_analysis_data_update() RETURNS trigger
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


ALTER FUNCTION public.procedure_analysis_data_update() OWNER TO postgres;

--
-- TOC entry 1258 (class 1255 OID 116484)
-- Name: procedure_analysis_update(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION procedure_analysis_update() RETURNS trigger
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


ALTER FUNCTION public.procedure_analysis_update() OWNER TO postgres;


--

CREATE TRIGGER trigger_analysis BEFORE INSERT OR UPDATE ON analysis FOR EACH ROW EXECUTE PROCEDURE procedure_analysis_update();



--

CREATE TRIGGER trigger_analysis_update BEFORE INSERT OR UPDATE ON analysis_data FOR EACH ROW EXECUTE PROCEDURE procedure_analysis_data_update();




