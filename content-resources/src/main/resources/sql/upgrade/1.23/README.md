## My Places visualizations

1. Replace my_places_categories table as done in the last SQL command of the file content-resources/src/main/resources/sql/PostgreSQL/create-myplaces-tables.sql.

2. Update GeoServer style file MyPlacesDefaultStyle.sld from content-resources/src/main/resources/sld/myplaces/. It is probably best to copy the style file manually to the /data/styles directory of GeoServer instead of a web interface because some web browsers might regard it as too large.

3. Check that the SansSerif.plain font is available in GeoServer. For example, load the Web Administration interface in the web browser and see Server Status -> Available fonts -> Full list of available fonts.

4. Copy a new WPS point clustering process file from geoserver-ext/wps/oskari_point_stacker/target/oskari_point_stacker-2.4.2.jar to the /WEB-INF/lib/ directory of your GeoServer installation. If needed, produce this jar file by running `mvn clean install` in the directory geoserver-ext/wps/oskari_point_stacker.

5. Restart GeoServer.

6. Load the GeoServer Web Administration Interface. If the extension was loaded properly, you should see "gs:OskariPointStacker" entry for WPS in the Service Capabilities (accessible from the rightmost column of the screen). If you don’t see this entry, check the logs for errors.

## New admin bundles

Run bundle registrations (you can check if they already exist in portti_bundle table) under content-resources/src/main/resources/sql/views/01-bundles/:

* admin/admin.sql
* framework/033-admin-users.sql

to register new admin bundles as they are now part of the default configuration for admin bundles.