package fi.nls.oskari.map.data.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GFIRestQueryParams extends GFIRequestParams {

    public static final double TOLERANCE_FACTOR = 0.002d; // from bbox width

    private static final String REST_GFI_BASE_PARAMS = "/query?where=&text=&objectIds=&time="
            + "&geometryType=esriGeometryEnvelope&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=*"
            + "&returnGeometry=false&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false"
            + "&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&"
            + "returnM=false&gdbVersion=&returnDistinctValues=false&f=json";


    private String getAsQueryString() {


        String name = getLayer().getName();   // Rest layer id

        try { // try encode
            name = URLEncoder.encode(getLayer().getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // encode unsupported then ignore it and use without encode
        }
        // &inSR=3857&geometry=2250000%2C8323000%2C2695000%2C8426000

        return name + REST_GFI_BASE_PARAMS + "&inSR=" + getSRSName().split(":")[getSRSName().split(":").length - 1] + "&geometry=" + getSearchBbox();

    }

    /**
     * Set searchbox
     * Based on width of current map window
     *
     * @return
     */

    private String getSearchBbox() {
        StringBuilder searchBox = new StringBuilder("");
        //Set searchbox
        try {
            String[] sbox = getBbox().split(",");
            if (sbox.length == 4) {
                double x1 = Double.valueOf(sbox[0]);
                double x2 = Double.valueOf(sbox[2]);
                double tol = (x2 - x1) * TOLERANCE_FACTOR;
                searchBox.append(String.valueOf(getLon() - tol) + ",");
                searchBox.append(String.valueOf(getLat() - tol) + ",");
                searchBox.append(String.valueOf(getLon() + tol) + ",");
                searchBox.append(String.valueOf(getLat() + tol));


            }
        } catch (Exception e) {

        }

        return searchBox.toString();
    }


    private String getBaseQueryURL() {
        String queryUrl = getLayer().getUrl();
        if (queryUrl.lastIndexOf("/export") > -1) queryUrl = queryUrl.replace("export", "");

        return queryUrl;
    }

    public String getGFIUrl() {
        return getBaseQueryURL() + getAsQueryString();
    }

}
