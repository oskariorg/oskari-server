# FAQ

##  Why there are no layers in a published map?

Check access rights. See [Oskari permissions](OskariPermissions.md)

##  Why there are broken tiles instead of my own places on the map?

Check that myplaces ajax url and namespace are properly configured.  See [Enabling My Places](EnablingMyPlacesWithPostgis.md) (sections 3 & 7)

##  Why are the myplaces timestamps not updated?
	
Check that you have added the trigger on myplaces table. This needs to be run manually and can be found in oskari-server/content-resources/src/main/resouces/sql/PostgreSQL/trigger-myplaces.sql

## How can I build Oskari with a new version tag?

Run `mvn -N versions:set -DnewVersion={NEW-VERSION}` on oskari-server root. It updates the version for oskari-server/pom.xml and all the maven modules defined in its modules-tag.

## Transport doesn't draw all geometries in tiles?

Check that the layers maxfeatures number is high enough in database. If the transport returns a tile, but some geometries are missing, usually its because there are more features than the maxfeatures count permits.
