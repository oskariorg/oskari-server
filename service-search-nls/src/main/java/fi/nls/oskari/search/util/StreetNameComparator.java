package fi.nls.oskari.search.util;

import fi.mml.portti.service.search.SearchResultItem;

import java.util.Comparator;

public class StreetNameComparator implements Comparator<SearchResultItem>{

	@Override
	public int compare(SearchResultItem o1, SearchResultItem o2) {
		if (o1 == null) {
			return 1;
		}
		
		if (o2 == null) {
			return -1;
		}
		
		if (o1.getTitle() == null) {
			return +1;
		}
		
		if (o2.getTitle() == null) {
			return -1;
		}
		
		String streetName1 = o1.getTitle().split("\\s")[0];
		String streetName2 = o2.getTitle().split("\\s")[0];
		
		/* Same street names  */
		if (streetName1.equals(streetName2)) {
			return o1.getTitle().length() - o2.getTitle().length();
		} else {
			return o1.getTitle().compareToIgnoreCase(o2.getTitle());
		}
		
	}

}
