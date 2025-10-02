CREATE TABLE myfeatures_layer (
	id uuid,
	feature_count int,
	opacity int,
	published boolean,
	created timestamp with time zone,
	updated timestamp with time zone,
	extent double precision ARRAY[4],
	owner_uuid varchar(64),
	locale json,
	fields json,
	options json,
	attributes json,
	CONSTRAINT pk_myfeatures_layer PRIMARY KEY (id)
);

CREATE TABLE myfeatures_feature (
	id bigint GENERATED ALWAYS AS IDENTITY,
	layer_id uuid,
	created timestamp with time zone,
	updated timestamp with time zone,
	fid varchar(128),
	geom geometry,
	properties json,
	CONSTRAINT pk_myfeatures_feature PRIMARY KEY (id)
);

ALTER TABLE ONLY myfeatures_feature ADD CONSTRAINT myfeatures_feature_myfeatures_layer_fkey FOREIGN KEY (layer_id) REFERENCES myfeatures_layer(id) ON DELETE CASCADE;