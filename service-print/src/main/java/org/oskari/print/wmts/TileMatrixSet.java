package org.oskari.print.wmts;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 6.6.2014
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
public class TileMatrixSet {

    private static final Logger log = LogFactory.getLogger(TileMatrixSet.class);

    private String id;
    private String crs;
    private Map<String, TileMatrix> tileMatrixMap = new HashMap<String, TileMatrix>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public void addTileMatrix(final TileMatrix matrix) {
        if(matrix == null) {
            return;
        }
        if(matrix.getId() == null || tileMatrixMap.get(matrix.getId()) != null) {
            log.warn("TileMatrix already exists or id missing!!!", matrix);
        }
        else {
            tileMatrixMap.put(matrix.getId(), matrix);
        }
    }

    public Map<String, TileMatrix> getTileMatrixMap() {
        return tileMatrixMap;
    }

    public void setTileMatrixMap(Map<String, TileMatrix> tileMatrixMap) {
        this.tileMatrixMap = tileMatrixMap;
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
