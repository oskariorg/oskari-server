
Caching tile blobs uses REDIS through Jedis to enable sharing blobs in a
in server cluster environment. 
 
Classes in package fi.nls.oskari.printout.caching.jedis are used to
    put/get tile blobs to/from REDIS.
    
Original intent was to use GeoWebCache to store blobs and there is some (unused) code for that. 