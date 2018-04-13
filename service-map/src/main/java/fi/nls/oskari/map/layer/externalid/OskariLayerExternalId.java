package fi.nls.oskari.map.layer.externalid;

import java.util.Objects;

public class OskariLayerExternalId {

    private final Integer layerId;
    private final String externalId;

    public OskariLayerExternalId(Integer layerId, String externalId) {
        this.layerId = Objects.requireNonNull(layerId);
        this.externalId = Objects.requireNonNull(externalId);
    }

    public Integer getLayerId() {
        return layerId;
    }

    public String getExternalId() {
        return externalId;
    }

}
