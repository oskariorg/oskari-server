package fi.mml.portti.domain.ogc.util;

import java.net.URL;

public class EpsgUrl {
    
    public URL getPropertyUrl() {
        return getClass().getResource("epsg.properties");
    }
    

}
