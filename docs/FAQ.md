# FAQ

##  Q: Why there are no layers in a published map?
	A: Check access rights. See [Oskari permissions](OskariPermissions.md)

##  Q: Why there are broken tiles instead of my own places on the map?
	A: Check that myplaces ajax url and namespace are properly configured.  See [Enabling My Places](EnablingMyPlacesWithPostgis.md) (sections 3 & 7)

##  Q: Why are the myplaces timestamps not updated?
	A: Check that you have added the trigger on myplaces table. This needs to be run manually and can be found in oskari-server/content-resources/src/main/resouces/sql/PostgreSQL/trigger-myplaces.sql

