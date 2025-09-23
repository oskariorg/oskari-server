package org.oskari.control.myfeatures.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.util.JSONHelper;

public class CreateMyFeaturesFeature {

    private UUID layerId;
    private String fid;
    private Geometry geometry;
    private Map<String, Object> properties;

    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (layerId == null) {
            errors.add("layerId is required");
        }
        if (geometry == null) {
            errors.add("geometry is required");
        }
        return errors;
    }

    public UUID getLayerId() {
        return layerId;
    }

    public MyFeaturesFeature toDomain(ObjectMapper om) throws Exception {
        MyFeaturesFeature feature = new MyFeaturesFeature();
        feature.setFid(fid);
        feature.setGeometry(geometry);
        if (properties != null) {
            feature.setProperties(JSONHelper.createJSONObject(om.writeValueAsString(properties)));
        }
        return feature;
    }

}
