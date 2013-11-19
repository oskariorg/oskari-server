# Standalone Jetty installation - Base for parcel application

This document helps to set up the environment. It does NOT instruct how to tune for performance or how things would be optimally configured for a production server.

Assumes pre-installed:
    * JDK 1.6+ (tested with 1.6.18)
    * Maven 3+ (tested with 3.0.5)
    * PostgresSQL 8.4+ (tested with 9.3) with a db created for Oskari

# Setting up Jetty Hightide 8.1.14

1) Download http://dist.codehaus.org/jetty/jetty-hightide-8.1.14/
2) unpack to selected location (referred as {jetty.home})
3) Configure database connection pool by adding the following snippet in {jetty.home}/etc/jetty.xml:

    <New id="OskariPool" class="org.eclipse.jetty.plus.jndi.Resource">
       <Arg></Arg>
       <Arg>jdbc/OskariPool</Arg>
       <Arg>
          <New class="org.postgresql.ds.PGSimpleDataSource">
             <Set name="User">postgres</Set>
             <Set name="Password">postgres</Set>
             <Set name="DatabaseName">oskaridb</Set>
             <Set name="ServerName">localhost</Set>
             <Set name="PortNumber">5432</Set>
          </New>
       </Arg>
    </New>

4) Edit the previous snippet to include actual database properties specific for your environment

5) Download PostgreSQL JDBC driver jar-file and copy it to {jetty.home}/lib/jdbc

    http://jdbc.postgresql.org/download/postgresql-9.3-1100.jdbc4.jar

6) Setup override properties for Oskari. Add an oskari-ext.properties in {jetty.home}/resources/oskari-ext.properties (oskari.trustAllCerts/oskari.trustAllHosts bypasses certificate errors on ssl requests):

    # set to true to get database populated with initial demo content
    oskari.init.db=false

    # true all ssl certs/hosts for debugging! configure certs on the server for production
    oskari.trustAllCerts=true
    # true all ssl certs/hosts for debugging! configure certs on the server for production
    oskari.trustAllHosts=true

    # url path to call for ajax requests/action routes
    oskari.ajax.url.prefix=/oskari-map/?

    ##############################
    # proxy services
    ##############################
    # adding property/parcel proxy
    oskari.proxyservices = property, print, wfsquery

    oskari.proxy.property.url=https://ws.nls.fi/ktjkii/wfs/wfs
    oskari.proxy.property.header.Host=ws.nls.fi
    oskari.proxy.property.header.Authorization=[setup auth header based on credentials]
    oskari.proxy.property.header.Content-Type=text/xml

    # Supported locales, comma separated and default first
    oskari.locales=fi_FI

---------------------------------------------

# Setting up static frontend code to be server by Jetty 

1) Go to jettys static content webapp

    cd {jetty.home}\webapps\root

2) Get the frontend code from github:

    git clone https://github.com/nls-oskari/oskari.git

3) Rename the created oskari folder to Oskari

    mv oskari Oskari

---------------------------------------------

# Packaging the backend 

1) Get the backend code from github:

    git clone https://github.com/nls-oskari/oskari-server.git

2) Compile and package the servlet by running 

    cd oskari-server
    mvn clean package -f servlet-map-pom.xml

3) Copy the war package from under oskari-server/servlet-map/target/ to {jetty.home}/webapps

-------------------------------------------

# Generate/populate database

1) Go to oskari-server/content-resources
2) Configure your database settings in content-resources/src/main/resources/db.properties:

    url=jdbc:postgresql://localhost:5432/oskaridb
    user=postgres
    pass=postgres

3a) Simple one-step setup

* Create the initial db with parcel view (this isn't perfect but gets you started)

    mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=postgres-parcel

OR 

3b) Step-by-step setup

    a ) Create and empty database by running (dropdb=true forces SQL execute even if there is existing tables):

        mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=create-empty-db

    b) Register bundles to the database by running:

        mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=postgres-register-bundles

    c) Add configurations for layers that you want to use in your application. View and parcel bundle refers to layer with certain IDs. These need to match with the inserted layers. To insert layers and permissions to view those layers see:

        content-resources/src/main/resources/sql/exampleLayersAndRoles.sql
        docs/ModifyingInitialDemoData.md
        docs/OskariPermissions.md

    d) Edit references to layer ids and queryUrl in

        content-resources/src/main/resources/json/views/parcel-as-default-view.json

    e) Add a default view (the view configuration referes to content-resources/src/main/resources/json/views/{addview param})

        mvn clean install exec:java -Doskari.addview=parcel-as-default-view.json



NOTES!! 

The provided parcel-as-default-view.json defines parcel action route as:

    "queryUrl" : "/oskari-map/?action_route=GetProxyRequest&serviceId=property",

This needs to be configured to an url responding to parcel loading

    "stickyLayerIds" : [99,90]

The layer ids also need to be configured to the system/configuration need to be changed