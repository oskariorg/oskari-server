Run "mvn clean install" to create a standalone runnable JAR-file in target folder.

Add an oskari-ext.properties file to the same dir as the JAR to override default settings like:

db.url=jdbc:postgresql://localhost:5432/oskaridb
db.username=postgres
db.password=admin