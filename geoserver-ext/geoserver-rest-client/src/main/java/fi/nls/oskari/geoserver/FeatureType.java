package fi.nls.oskari.geoserver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by SMAKINEN on 1.9.2015.
 */
@JsonRootName("featureType")
public class FeatureType {
    public String name;
    public boolean enabled = true;
    public String srs;
    public String nativeCRS;
    public Map<String, Double> nativeBoundingBox = new HashMap<>();

    @JsonIgnore
    public void setBounds(double minx, double maxx, double miny, double maxy) {
        nativeBoundingBox.put("minx", minx);
        nativeBoundingBox.put("maxx", maxx);
        nativeBoundingBox.put("miny", miny);
        nativeBoundingBox.put("maxy", maxy);
    }

}
