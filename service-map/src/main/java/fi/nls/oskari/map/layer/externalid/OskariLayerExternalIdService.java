package fi.nls.oskari.map.layer.externalid;

public interface OskariLayerExternalIdService {

    public OskariLayerExternalId findByExternalId(String externalId);
    public int insert(OskariLayerExternalId link);
    public int delete(int layerId);

}
