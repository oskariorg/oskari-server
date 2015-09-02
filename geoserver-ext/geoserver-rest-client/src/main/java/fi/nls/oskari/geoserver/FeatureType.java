package fi.nls.oskari.geoserver;

import com.fasterxml.jackson.annotation.JsonRootName;


/**
 * Created by SMAKINEN on 1.9.2015.
 */
@JsonRootName("featureType")
public class FeatureType {
    public String name;
    public boolean enabled = true;
    public String srs;
    public String projectionPolicy = "FORCE_DECLARED";

}
