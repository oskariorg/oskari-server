# omat_paikat database

## Initialize my_places.updated column to null in row INSERT action:

Initial updated value was "01.01.1970" in insert action (AH-1341):

1. Build frontend
2. Drop Not Null in my_places.updated column (01-alter_my_places_update_drop_not_null.sql)
3. Refresh in GeoServer admin my_places layer fields ( layer reload types...  ans Save)

