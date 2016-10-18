--add "created" field to ratings table
ALTER TABLE ratings ADD COLUMN created timestamp without time zone NOT NULL DEFAULT now();
