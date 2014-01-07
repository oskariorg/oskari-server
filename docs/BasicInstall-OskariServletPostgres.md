# Standalone Jetty installation - Base for Oskari application with Postgres

This document helps to set up the environment. It does NOT instruct how to tune for performance or how things would be optimally configured for a production server.

Assumes pre-installed:

* JDK 1.6+ (tested with 1.6.18)
* Cygwin32 or 64, if windows environment (Windows 7 tested)
* Maven 3+ (tested with 3.0.5)
* PostgresSQL 8.4+ (tested with 9.3) with a oskaridb created for Oskari

# Create oskaridb data base with pgAdmin or with psql

     CREATE DATABASE oskaridb
     WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;

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
             <Set name="Password">[your pw]</Set>
             <Set name="DatabaseName">oskaridb</Set>
             <Set name="ServerName">localhost</Set>
             <Set name="PortNumber">5432</Set>
          </New>
       </Arg>
    </New>

4) Edit the previous snippet to include actual database properties specific for your environment

5) Download PostgreSQL JDBC driver jar-file and copy it to {jetty.home}/lib/jdbc

    http://jdbc.postgresql.org/download/postgresql-9.3-1100.jdbc4.jar

6) Set default jetty.port for jetty run in etc/jetty.xml file

     <Call name="addConnector">
      <Arg>
          <New class="org.eclipse.jetty.server.nio.SelectChannelConnector">
            <Set name="host"><Property name="jetty.host" /></Set>
            <Set name="port"><Property name="jetty.port" default="8888"/></Set>



---------------------------------------------

# Setting up static frontend code to be server by Jetty 

1) Go to jettys static content webapp

    cd {jetty.home}\webapps\root

2) Get the frontend code from github:

    git clone https://github.com/nls-oskari/oskari.git

3) Rename the created oskari folder to Oskari

    mv oskari Oskari

---------------------------------------------

# Setting up and packaging the oskari backend 

1) Get the backend code from github:

    git clone https://github.com/nls-oskari/oskari-server.git



2) Run all Maven commands listed in *oskari-server/external-libs/mvn-install.txt*

    This adds maven dependencies not found in common repositories to your local maven repository



3) Uncomment postgress dependency in servlet-map/pom.xml:
    <dependency>
        <groupId>postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>8.4-702.jdbc4</version>
    </dependency>

4) Comment out the hsqldb dependency in servlet-map/pom.xml:
    <dependency>
        <groupId>hsqldb</groupId>
        <artifactId>hsqldb</artifactId>
        <version>1.8.0.7</version>
    </dependency>

5) Compile and package the servlet by running 

    cd oskari-server
    mvn clean package -f servlet-map-pom.xml

6) Copy the war package from under oskari-server/servlet-map/target/ to {jetty.home}/webapps

7) Setup override properties for Oskari. Add an oskari-ext.properties in {jetty.home}/resources/oskari-ext.properties (oskari.trustAllCerts/oskari.trustAllHosts bypasses certificate errors on ssl requests):

   Copy oskari-server/servlet-map/src/main/resources/oskari.properties to {jetty.home}/resources/oskari-ext.properties

   Replace below property values in . properties file

    # set to true to get database populated with initial demo content
    oskari.init.db=false

    # true all ssl certs/hosts for debugging! configure certs on the server for production
    oskari.trustAllCerts=true
    # true all ssl certs/hosts for debugging! configure certs on the server for production
    oskari.trustAllHosts=true

    # url path to call for ajax requests/action routes
    oskari.ajax.url.prefix=/oskari-map/?

    # Supported locales, comma separated and default first
    oskari.locales=fi_FI

-------------------------------------------

# Generate/populate database

1) Go to oskari-server/content-resources

2) Configure your database settings in content-resources/src/main/resources/db.properties:

    url=jdbc:postgresql://localhost:5432/oskaridb
    user=postgres
    pass=[your pw]

3) Simple one-step setup

Create the initial db with default view (this isn't perfect but gets you started) 

    mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=postgres-default

-------------------------------------------

# Startup application

1) Startup Jetty  (cmd.exe in Windows)

    cd {jetty.home}
    java -jar start.jar
   
   (or start with proxy setup, if any  eg. java -jar start.jar -Dhttp.proxyHost=wwwp.xxx.xx -Dhttp.proxyPort=800 -Dhttp.nonProxyHosts="*.yyy.xx|*.foo.fi" --exec)

2) Start application
   *http://localhost:8888/oskari-map* in your browser

   You can login with username "user" and password "user" as a normal user or "admin"/"oskari" as an admin user (no real difference yet)