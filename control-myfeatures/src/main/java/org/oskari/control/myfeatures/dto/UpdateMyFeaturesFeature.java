package org.oskari.control.myfeatures.dto;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;

public class UpdateMyFeaturesFeature extends CreateMyFeaturesFeature {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> validate() {
        List<String> errors = super.validate();
        if (id == null) {
            errors.add("id is required");
        }
        return errors;
    }

    public MyFeaturesFeature toDomain(ObjectMapper om) throws Exception {
        MyFeaturesFeature feature = super.toDomain(om);
        feature.setId(id);
        return feature;
    }

}
