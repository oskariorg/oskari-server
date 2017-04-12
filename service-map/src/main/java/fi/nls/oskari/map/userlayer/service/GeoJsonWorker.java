package fi.nls.oskari.map.userlayer.service;

import org.json.JSONObject;
import org.opengis.feature.type.FeatureType;

import java.io.File;

public interface GeoJsonWorker {
    public JSONObject getGeoJson();
    public String getTypeName();
    public FeatureType getFeatureType();
    public String parseGeoJSON(File file, String source_epsg, String target_epsg);

       
}
