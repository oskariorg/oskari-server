package org.oskari.print.wmts;

public interface GetTileRequestBuilder {

    public GetTileRequestBuilder tileRow(int tileRow);
    public GetTileRequestBuilder tileCol(int tileCol);
    public String build();

}
