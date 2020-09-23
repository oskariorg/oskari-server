package fi.mml.portti.service.search;

import java.util.Comparator;

public class AreaSizeComparator implements Comparator<SearchResultItem> {

    public int compare(SearchResultItem first, SearchResultItem second) {
        return getArea(first) - getArea(second);
    }

    private int getArea(SearchResultItem item) {
        return (int) Math.round(
                (item.getWestBoundLongitude() - item.getEastBoundLongitude())
                *
                (item.getNorthBoundLatitude() - item.getSouthBoundLatitude()));
    }
}
