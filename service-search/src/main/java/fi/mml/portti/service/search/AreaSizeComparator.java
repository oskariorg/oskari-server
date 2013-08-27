package fi.mml.portti.service.search;

import java.util.Comparator;

public class AreaSizeComparator implements Comparator<SearchResultItem> {

	public int compare(SearchResultItem sri1, SearchResultItem sri2){

		float left1 = Float.parseFloat(sri1.getEastBoundLongitude());
		float right1 = Float.parseFloat(sri1.getWestBoundLongitude());
		float bottom1 = Float.parseFloat(sri1.getSouthBoundLatitude());
		float top1 = Float.parseFloat(sri1.getNorthBoundLatitude());
		
		float left2 = Float.parseFloat(sri2.getEastBoundLongitude());
		float right2 = Float.parseFloat(sri2.getWestBoundLongitude());
		float bottom2 = Float.parseFloat(sri2.getSouthBoundLatitude());
		float top2 = Float.parseFloat(sri2.getNorthBoundLatitude());
	
		int areaSize1 = Math.round((right1-left1)* (top1-bottom1));
		int areaSize2 =  Math.round((right2-left2)* (top2-bottom2));
		
		return areaSize1-areaSize2;

	}
}
