package org.oskari.control.myfeatures.dto;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;

public class UpdateMyFeaturesLayer extends CreateMyFeaturesLayer {

    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public List<String> validate() {
        List<String> errors = super.validate();
        if (id == null) {
            errors.add("id is required");
        }
        return errors;
    }

    @Override
    public MyFeaturesLayer toDomain(ObjectMapper om) throws Exception {
        MyFeaturesLayer l = super.toDomain(om);
        l.setId(id);
        return l;
    }

}
