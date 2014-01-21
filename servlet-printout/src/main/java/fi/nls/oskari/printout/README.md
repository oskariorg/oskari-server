
Code in this package and subpackages implement map printout functionality
for oskari frontend and oskari-backend.

This is view snapshot as raster - not a real map output. 
Map scales equal to those shown on screen and are not very useful printed.    

# Example Request Processing Details 

 Example maplink based PNG image request
     /oskari-printout-backend-1.0.1-SNAPSHOT
         /imaging/service/thumbnail/maplink.png?
            zoomLevel=9&coord=385560_6675172&mapLayers=24+100+&
            pageSize=A3_Landscape&pageLogo=true&pageTitle=JOO
            
Maplink based imaging request is processed by JAX-RS engine Jersey
 fi.nls.oskari.printout.ws.jaxrs.resource.MapResource#getSnapshotPNG(@Context UriInfo ui)
   class method is invoked
   
   MapResource class does only basic parameter mapping and dispatches control to a
   shared instance of WebServiceMapProducerResource. 
   
   WebServiceMapProducerResource#getMapPNG() is called with request information.
   
   A new MapProducer instance is created for request processing.
   - MapProducer creates an instance of MapLinkWorkingSetProcessor and MapLinkWorkingSetTileBreeder 
     which will be used to calculate image tiles required to cover the extent of the map request.
   - GeoTools MapContent, MapViewport and StreamingRenderer are created for output generation 
   
   The Maplink is parsed.
   
   StreamingPNGImpl instance is created to get response as PNG image and control
   is dispatched to StreamingPNGImpl#underflow()
   
   Further request argument mapping to map producer argument is taking place.
   
   A AsyncLayerProcessor instance is created to process imag tile requests.  
   
   MapProducer#getMap() and calls to following methods create the actual content of the result image. 
   -    MapProducer#buildLayers();
   -    MapProducer#buildLayerTiles()
   -    MapProducer#buildMapImage();
   
   The PNG image created is streamed to when JAX-RS engine invokes StreamingPNGImpl#write(). 
     
        
 
