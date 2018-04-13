package fi.nls.oskari.map.layer.externalid;

import java.util.Objects;

public class OskariLayerExternalId {

    private final int layerId;
    private final String externalId;

    public OskariLayerExternalId(Integer layerId, String externalId) {
        this.layerId = layerId;
        this.externalId = Objects.requireNonNull(externalId);
    }

    public int getLayerId() {
        return layerId;
    }

    public String getExternalId() {
        return externalId;
    }

}
