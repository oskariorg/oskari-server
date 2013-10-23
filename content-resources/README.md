# Populating database content

Database properties can be configured in src/main/resources/db.properties

After that the database can be populated with maven running:

    mvn clean install exec:java -Doskari.dropdb=true

You can configure the setup file to run with -Doskari.setup=[setup file under resources/setup] like -Doskari.setup=postgres-add-myplaces

# Setup file structure

Setup file can have 4 segments: "create", "bundles", "views" and "sql". These are run in the listed order.

Create is used to create tables.

Bundles refers to bundle registration. It runs the sql files under /src/main/resources/sql/views/01-bundles/[namespace]/[bundle.sql].

Views are created after bundles are registered. Any bundle refered to in a view configuration need to be registered in the previous step.
Views are configured as json under /src/main/resources/json/views/[view.json]

Sql should be generic sql statements that add map layers/permissions and other content.