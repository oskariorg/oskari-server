-- SLD assumes that the dasharray is empty instead of null
-- with null border is ALWAYS dash
-- with '' solid border AND dash works properly

CREATE OR REPLACE FUNCTION procedure_categories_update()
  RETURNS TRIGGER AS
$BODY$
BEGIN
    IF NEW.stroke_dasharray is Null THEN
        NEW.stroke_dasharray = '';
    END IF;
    IF NEW.border_dasharray is Null THEN
        NEW.border_dasharray = '';
    END IF;
	
    RETURN NEW;
 END;
 $BODY$
 LANGUAGE 'plpgsql' VOLATILE;

 
DROP TRIGGER IF EXISTS trigger_categories_update ON categories;

CREATE TRIGGER trigger_categories_update
BEFORE INSERT OR UPDATE
ON categories
FOR EACH ROW
EXECUTE PROCEDURE procedure_categories_update();