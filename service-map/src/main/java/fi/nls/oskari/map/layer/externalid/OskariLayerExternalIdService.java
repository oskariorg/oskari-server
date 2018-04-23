package fi.nls.oskari.map.layer.externalid;

public interface OskariLayerExternalIdService {

    public Integer findByExternalId(String externalId);
    public void delete(int layerId);

}
