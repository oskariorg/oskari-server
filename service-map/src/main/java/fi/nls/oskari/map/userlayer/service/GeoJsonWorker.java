package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.analysis.AnalysisAndStyle;
import fi.nls.oskari.map.userlayer.domain.GeoJsonCollection;
import fi.nls.oskari.service.db.BaseService;
import org.json.JSONObject;
import org.opengis.feature.type.FeatureType;

import java.io.File;

public interface GeoJsonWorker {
    public JSONObject getGeoJson();
    public String getTypeName();
    public FeatureType getFeatureType();
    public boolean parseGeoJSON(File file);

       
}
