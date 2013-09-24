package fi.mml.portti.service.search;

import java.util.Comparator;

public class TitleComparator implements Comparator<SearchResultItem>{
	
	@Override
	public int compare(SearchResultItem sri1, SearchResultItem sri2){

	//parameter are of type Object, so we have to downcast it to SearchResultItem objects

	String sri1Title = sri1.getTitle();

	String sri2Title = sri2.getTitle();
	
	// to prevent NullPointerExceptions
	if (sri1Title == null) {
		return 1;
	} else if (sri2Title == null) {
		return -1;
	}
	
	//uses compareTo method of String class to compare titles of the result item

	return sri1Title.compareTo(sri2Title);

	}
}
