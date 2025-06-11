CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR "org.h2gis.functions.factory.H2GISFunctions.load";
CALL H2GIS_SPATIAL();

CREATE TABLE myfeatures_layer (
	id uuid,
	feature_count int,
	created timestamp with time zone default current_timestamp,
	updated timestamp with time zone default current_timestamp,
	extent double precision ARRAY[4],
	owner_uuid varchar(64),
	locale json,
	fields json,
	options json,
	attributes json,
	CONSTRAINT pk_myfeatures_layer PRIMARY KEY (id)
);

CREATE TABLE myfeatures_feature (
	layer_id uuid,
	fid varchar(128),
	geom geometry,
	properties json,
	created timestamp with time zone default current_timestamp,
	updated timestamp with time zone default current_timestamp,
	CONSTRAINT pk_myfeatures_feature PRIMARY KEY (layer_id, fid)
);

ALTER TABLE myfeatures_feature ADD CONSTRAINT fk_myfeatures_feature_myfeatures_layer FOREIGN KEY (layer_id) REFERENCES myfeatures_layer(id) ON DELETE CASCADE;
