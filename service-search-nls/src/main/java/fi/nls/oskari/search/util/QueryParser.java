package fi.nls.oskari.search.util;

import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.nls.oskari.util.ConversionHelper;

/**
 * This class parses a query.
 * 
 * <p>The following types of search strings are currently supported:</p>
 * <ul>
 * <li>Mannerheimintie</li>
 * <li>Mannerheimintie 12</li>
 * <li>Mannerheimintie 12A</li>
 * <li>Mannerheimintie 12 A</li>
 * <li>Mannerheimintie 10-14</li>
 * <li>Mannerheimintie 12 Helsinki</li>
 * <li>Mannerheimintie Helsinki</li>
 * <li>erik bassen tie 4 espoo</li>
 * <li>mannerh*</li>
 * </ul>
 * 
 * <p>where</p>
 * <ul>
 * <li>Mannerheimintie - street name, must not contain spaces</li>
 * <li>12 - house number</li>
 * <li>10-14 - house number from to</li>
 * <li>Helsinki - village name</li>
 * </ul>
 * 
 * <p>If search string contains commas, they are removed.</p>
 * 
 * <p>Query is illegal, if</p>
 * <ul>
 * <li>streetName is missing</li>
 * <li>streetName ends with * and its length is four characters or less (including *)</li>
 * <li>streetName contains * but it is not in the end</li>
 * <li>streetName contains more than one *'s</li>
 * <li>villageName contains *</li>
 * <li>houseNumber contains *</li>
 * <li>if villageName or villageNumber have been set, streetName must not contain *</li>
 * <li>unknown village name</li>
 * </ul>
 */
public class QueryParser {

	private String query;
	private String streetName = "";
	private String houseNumber = null;
	private String villageName  = null;
	private String villageNumber = null;

	public QueryParser(String query) {
		this.query = query.trim();
	}

	public String toString() {
		return "query='" + query + "', streetName=" + streetName + ", houseNumber=" 
		+ houseNumber + ", villageName=" + villageName 
		+ ", villageNumber=" + villageNumber;
	}

	public String getQuery() {
		return query;
	}

	public String getStreetName() {
		return streetName;
	}

	public String getHouseNumber() {
		return houseNumber;
	}

	public String getVillageName() {
		return villageName;
	}

	public String getVillageNumber() {
		return villageNumber;
	}

	/**
	 * Parse the query.
	 * 
	 * @throws IllegalSearchCriteriaException if query is illegal
	 */
	public void parse() throws IllegalSearchCriteriaException {
		if ((query == null) || "".equals(query)) {
			return;
		}		
		query = query.replaceAll(", ", " ").replaceAll("_", " ");
		String[] queryParts = query.replaceAll(",", " ").split("\\s");		       
		
		int villageIndex = getVillageIndex(queryParts);
		
		if (villageIndex > -1) {
			villageName = queryParts[villageIndex];
		}
		
		int houseNroIndex = getHouseNroIndex(queryParts);
		
		if (houseNroIndex > -1) {
			houseNumber = queryParts[houseNroIndex];
		}
		
		int houseStairIndex = getStairIndex(queryParts, villageIndex);
		
		if (houseStairIndex > -1) {
			houseNumber += queryParts[houseStairIndex];
		}
		
		for (int i = 0; i < queryParts.length; i++) {
			if (i != villageIndex && i != houseNroIndex && i != houseStairIndex) {
				//System.out.println(queryParts[i]);
				if (!"".equals(queryParts[i]) && !" ".equals(queryParts[i]) &&
					!isHouseNro(queryParts[i]) && !isStair(queryParts[i])) {
					streetName +=" "+ queryParts[i];
				}
			}
		}
		if (streetName.length() > 0) {
			streetName = streetName.substring(1);
		}
		
		isQueryIllegal();
	}
	
	private int getVillageIndex(String[] queryParts) {
		
		for (int i = 0; i < queryParts.length; i++) {
			
			//System.out.println("queryParts:"+ queryParts[i]);
			if (!"".equals(queryParts[i])) { 
				String villageCode = getVillageCode(queryParts[i]);
				
				if (!"".equals(villageCode)) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	private int getHouseNroIndex(String[] queryParts) {
		
		for (int i = 0; i < queryParts.length; i++) {
			
			if (!"".equals(queryParts[i]) && isHouseNro(queryParts[i])) {
				return i;
			}
		}
		
		return -1;
	}
	
	private boolean isHouseNro(String testing) {
		return testing.matches("\\d+|\\d+[a-z,A-Z]||\\d+[a-z,A-Z][a-z,A-Z]|\\d+-\\d+");
	}
	
	private boolean isStair(String testing) {
		return testing.matches("[a-z,A-Z]||[a-z,A-Z][a-z,A-Z]");
	}
	
	private int getStairIndex(String[] queryParts,int villageIndex) {
		
		for (int i = 0; i < queryParts.length; i++) {
			
			if (!"".equals(queryParts[i]) && i != villageIndex && isStair(queryParts[i])) {
				return i;
			}
		}
		
		return -1;
	}
	

	/**
	 * Check if this query is illegal.
	 */
	private void isQueryIllegal() throws IllegalSearchCriteriaException {		

		if (streetName.endsWith("*")) {
			if (streetName.length() <= 4) {
				throw new IllegalSearchCriteriaException(
						"streetName ends with * and its length is four characters or less (including *). Query: " 
						+ query);
			}
		} else if (containsStar(streetName)) {
			throw new IllegalSearchCriteriaException("streetName contains * which it is not in the end. Query: " 
					+ query);
		}

		if (ConversionHelper.count(streetName, "*") > 1) {
			throw new IllegalSearchCriteriaException("streetName contains more than one * characters. Query: " 
					+ query);
		}

		if (containsStar(villageName) || containsStar(houseNumber)) {
			throw new IllegalSearchCriteriaException("villageName or houseNumber contains *. Query: " + query);
		}

		if ((villageNumber != null ) && !"".equals(villageNumber) && (streetName.indexOf("*") >= 0)) {
			if (streetName.indexOf("*") >= 0) {
				throw new IllegalSearchCriteriaException(
						"If villageName or villageNumber have been set, streetName must not contain *. Query: " 
						+ query);
			}
		}		
	}

	private boolean containsStar(String someString) {
		if (someString != null && someString.indexOf("*") >= 0) {
			return true;
		}
		return false;
	}

	
	/**
	 * Returns village code from utils
	 * 
	 * @param villageName
	 * @return
	 */
	protected String getVillageCode(String villageName) {
		return SearchUtil.getVillageCode(villageName);
	}

}
