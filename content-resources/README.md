# DB populator

## Database setting

The database properties can be modified in oskari-server/content-resources/src/main/resources/db.properties.

Also these database-settings can be overridden by using -Doskari.env=myenv parameter on maven call.
This reads db.properties as base, reads an additional properties file db-myenv.properties and overrides any keys found in base properties with the env-specific properties.

Finally it is possible to provide properties using any .properties file by specifying the path to the file
as the first command line argument. This is useful if you wish to run the DB populator as a standalone unmodified
.jar file.

## Populating database content

After the database connection parameters have been configured the database can be populated with maven running (in oskari-server/content-resources):

    mvn clean install exec:java -Doskari.dropdb=true

NOTE! 'oskari.dropdb=true' doesn't actually mean that the DB is dropped as is. The DB handler checks at the beginning if the DB has any tables it recognizes.
If tables exist the setup-file is NOT run. The setup-file can be FORCED to run with 'oskari.dropdb=true'. Setup-files CAN drop DB tables so it's important to know what you are doing,
hence the  safety measure.

# Setup files

You can configure a specific setup file to run by adding a parameter -Doskari.setup=[setup file under resources/setup].
The setup file that is used if the parameter is NOT defined is '[default](src/main/resources/setup/app-default.json)'.

The parameter '-Doskari.setup=postgres-default' references a setup file located in oskari-server/content-resources/src/main/resources/setup/

The value of the parameter is the filename without extension so the actual file referenced is 'app-default.json' in the above link.

Setup file can have 5 segments: "create", "setup", "bundles", "views" and "sql". These are run in the listed order.

## Create

Used to create tables and setup the database so content can be added.

## Setup

Used to recurse setup files. For example creating empty db and registering bundles is pretty much in each setup file as well as
separate setup files for creating the database step-by-step. With the setup-array a series of "minisetups" can be run from another setup which results in less boilerplate.

## Bundles

Bundle registration. The bundles are registered by running the sql files under oskari-server/content-resources/src/main/resources/sql/views/01-bundles/[namespace]/[bundle.sql].

## Views

Views are created after bundles are registered. Any bundle refered to in a view configuration need to be registered in the previous step.
Views are configured as json under oskari-server/content-resources/src/main/resources/json/views/[view.json] and are ran in the order they appear in the setup-file.

The views are created based on the JSON files which try to minimize copy/pasting boilerplate.
This means that when the view JSON references bundles it's enough to only tell the id for the bundle to use.
When the view is created the bundle configuration is read from the registered bundles (database table portti_bundle).
After that the bundle config/startup/state is overwritten with the value in the JSON file IF they are defined (if not, the values from portti_bundle will be used).

## Layers

Additional layer definitions can be listed here. These are not visible by default unlike those listed in "selectedLayers"
within individual view definitions.

## Sql

Array of generic sql statements to add map layers/permissions and other content.
These are similar to the ones in create-step but now we can assume tables are created, bundles are registered and views created.

# Adding a new view

Views can be added without running whole setup-files. Add a postgres sample view with the following command:

    mvn clean install exec:java -Doskari.addview=postgres-sample-view.json

The view JSON is parsed and added as view to the db as it would have been if it had been referenced in a setup-file.

# Adding a layer

Layers can be added without running whole setup-files. Add a sample layer with the following command:

    mvn clean install exec:java -Doskari.addlayer=arcgis-background-map-fi.json

The layer JSON is parsed and added as layer to the db as it would have been if it had been referenced in a setup/view-file.
Referenced files have base dir src/main/resources/json/layers.

# Resource overlay files

You can place new or modified setup files in an external directory tree that follows the same structure
as the files under the resource directory. You must provide the path to this directory using the parameter
-Doskari.resourceOverlayDir=/path/to/your/overlay/directory. This makes it possible to use the DB populator
as a standalone tool without modifying the resources contained in it and store your application specific
configuration elsewhere:

    mvn assembly:assembly
    java "-Doskari.dropdb=true" "-Doskari.setup=yourapp" "-Doskari.resourceOverlayDir=c:/your/overlay" \
    -jar target/content-resources-VERSION-jar-with-dependencies.jar c:/your/db/env.properties
