package fi.nls.oskari.map.userlayer.service;

import org.json.JSONObject;
import org.opengis.feature.type.FeatureType;

import java.io.File;

public interface GeoJsonWorker {
    public JSONObject getGeoJson();
    public String getTypeName();
    public FeatureType getFeatureType();
    public boolean parseGeoJSON(File file, String target_epsg);

       
}
