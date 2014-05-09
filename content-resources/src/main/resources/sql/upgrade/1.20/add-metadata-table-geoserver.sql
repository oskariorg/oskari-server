
-- Create primary key table for GeoServer
CREATE TABLE gt_pk_metadata_table
(
  table_schema character varying(32) NOT NULL,
  table_name character varying(32) NOT NULL,
  pk_column character varying(32) NOT NULL,
  pk_column_idx integer,
  pk_policy character varying(32),
  pk_sequence character varying(64),
  CONSTRAINT gt_pk_metadata_table_table_schema_table_name_pk_column_key UNIQUE (table_schema, table_name, pk_column)
)
WITH (
OIDS=FALSE
);
ALTER TABLE gt_pk_metadata_table
OWNER TO liferay;


INSERT INTO gt_pk_metadata_table(
  table_schema, table_name, pk_column, pk_column_idx, pk_policy,
  pk_sequence)
  VALUES (
    'public',
    'vuser_layer_data',
    'id',
    null,
    'assigned',
    null);
