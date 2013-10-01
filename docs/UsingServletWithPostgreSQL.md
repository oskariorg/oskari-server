# Using servlet with postgresql

1) Install PostgreSQL: http://www.postgresql.org/ (tested with version 9.3)

2) create a database with name oskaridb and change the connection pool in jetty-env.xml

    <New id="oskaridb" class="org.mortbay.jetty.plus.naming.Resource">
        <Arg>jdbc/OskariPool</Arg>
        <Arg>
            <New class="org.apache.commons.dbcp.BasicDataSource">
                <Set name="driverClassName">org.postgresql.Driver</Set>
                <Set name="url">jdbc:postgresql://localhost:5432/oskaridb</Set>
                <Set name="username">*username*</Set>
                <Set name="password">*password*</Set>
            </New>
        </Arg>
    </New>

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

5) Start the server with `mvn clean install -f servlet-map-pom.xml -Doskari.dropdb=true -Doskari.setup=postgres-default`

6) Open your browser with http://localhost:2373/?viewId=2 to see a new view loaded from the database.
    You should now see personaldata and publisher bundles started compared to the default view

7) Adding [admin bundles for admin role](AddingBundlesBasedOnRole.md)