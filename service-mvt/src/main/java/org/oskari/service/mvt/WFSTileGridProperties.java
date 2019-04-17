package org.oskari.service.mvt;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WFSTileGridProperties {

    private static final Logger LOG = LogFactory.getLogger(WFSTileGridProperties.class);
    private static final String WFS_MVT_PROPERTY_NAMESPACE = "oskari.wfs.mvt";

    private Map<String, WFSTileGrid> tileGridMap;

    public WFSTileGridProperties () {
        tileGridMap = new HashMap<>();
        final String[] srs = PropertyUtil.getCommaSeparatedList(WFS_MVT_PROPERTY_NAMESPACE + ".srs");
        if (srs.length != 0) {
            Arrays.stream(srs).forEach(cur -> addTileGrid(cur));
        }
    }

    private void addTileGrid (String srs) {
        String srsCode = srs.toUpperCase();
        String srsNamespace = WFS_MVT_PROPERTY_NAMESPACE + "." + srsCode.replace("EPSG:", "");
        try {
            // Parse from properties file
            String maxZoomPropName = srsNamespace + ".maxZoomLevel";
            int maxZoomLevel = Integer.parseInt(PropertyUtil.get(maxZoomPropName));

            String extentPropName = srsNamespace + ".extent";
            final String[] extentStr = PropertyUtil.getCommaSeparatedList(extentPropName);
            double [] extent = Arrays.stream(extentStr).mapToDouble(num -> Double.parseDouble(num)).toArray();

            WFSTileGrid tileGrid = new WFSTileGrid(extent, maxZoomLevel);
            tileGridMap.put(srsCode, tileGrid);
        }
        catch (Exception ex) {
            LOG.error("Couldn't add WFS MVT tile grid: " + srs, ex);
        }
    }

    public WFSTileGrid getTileGrid(String srs) {
        return tileGridMap.get(srs.toUpperCase());
    }

    public Map<String, WFSTileGrid> getTileGridMap() {
        return tileGridMap;
    }

}
