package fi.nls.oskari.wmts.domain;

import fi.nls.oskari.util.JSONHelper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Immutable Java POJO presentation of <element name="TileMatrixSet">
 * @see http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd
 * Does not support BoundingBox and WellKnownScaleSet elements
 */
public class TileMatrixSet {

    private final String id;
    private final String crs;
    private final Map<String, TileMatrix> tileMatrixMap;

    public TileMatrixSet(String id, String crs, List<TileMatrix> tileMatrices)
            throws IllegalArgumentException {
        this.id = id;
        this.crs = crs;
        this.tileMatrixMap = tileMatrices.stream()
                .collect(Collectors.toMap(TileMatrix::getId, tm -> tm));
        validate();
    }

    private void validate() throws IllegalArgumentException {
        if (this.id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Missing id");
        }
        if (this.crs == null || crs.isEmpty()) {
            throw new IllegalArgumentException("Missing SupportedCrs");
        }
        if (this.tileMatrixMap == null || tileMatrixMap.isEmpty()) {
            throw new IllegalArgumentException("Missing TileMatrix");
        }
    }

    public String getId() {
        return id;
    }

    public String getCrs() {
        return crs;
    }

    public Map<String, TileMatrix> getTileMatrixMap() {
        return tileMatrixMap;
    }

    public JSONObject getAsJSON() {
        final JSONObject obj = new JSONObject();
        JSONHelper.putValue(obj, "identifier", getId());
        JSONHelper.putValue(obj, "projection", getCrs());

        final JSONArray matrixIds = new JSONArray();
        for(TileMatrix matrix : getTileMatrixMap().values()) {
            matrixIds.put(matrix.getAsJSON());
        }
        JSONHelper.putValue(obj, "matrixIds", matrixIds);

        return obj;
    }
}
