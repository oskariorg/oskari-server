package org.oskari.print.wmts;

import java.util.Map;

public interface GetTileRequestBuilder {

    public GetTileRequestBuilder additionalParams(Map<String, Object> params);
    public GetTileRequestBuilder tileRow(int tileRow);
    public GetTileRequestBuilder tileCol(int tileCol);
    public String build();

}
