package fi.nls.oskari.map.userlayer.domain;


import org.json.JSONObject;
import org.opengis.feature.type.FeatureType;

public abstract class  GeoJsonCollection {


   private JSONObject geoJson = null;
   private String typeName = "";
   private FeatureType featureType = null;

    public JSONObject getGeoJson() {
        return geoJson;
    }

    public void setGeoJson(JSONObject geoJson) {
        this.geoJson = geoJson;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public void setFeatureType(FeatureType featureType) {
        this.featureType = featureType;
    }
}
