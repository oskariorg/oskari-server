package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.net.URLEncoder;

@Oskari(GeoNamesSearchChannel.ID)
public class GeoNamesSearchChannel extends SearchChannel {

    /** logger */
    private Logger log = LogFactory.getLogger(this.getClass());
    private String serviceURL = null;
    public static final String ID = "GEONAMES_CHANNEL";
    private static final String PROPERTY_SERVICE_URL = "search.channel.GEONAMES_CHANNEL.service.url";

    @Override
    public void init() {
        super.init();
        serviceURL = PropertyUtil.getOptional(PROPERTY_SERVICE_URL);
        log.debug("ServiceURL set to " + serviceURL);
    }

    /**
     * Returns the search raw results. 
     * @param searchCriteria Search criteria.
     * @return Result data in JSON format.
     * @throws Exception
     */
    private JSONArray getData(SearchCriteria searchCriteria) throws Exception {
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key",PROPERTY_SERVICE_URL);
            return new JSONArray();
        }
        StringBuffer buf = new StringBuffer(serviceURL);

        buf.append("?name=");
        buf.append(URLEncoder.encode(searchCriteria.getSearchString(),"UTF-8"));
        buf.append("&lang=");
        buf.append(searchCriteria.getLocale());
        // Get all admin names
        buf.append("&style=FULL");
        // Country filter
        buf.append("&country=CA");
        buf.append("&country=DK");
        buf.append("&country=FI");
        buf.append("&country=IS");
        buf.append("&country=NO");
        buf.append("&country=RU");
        buf.append("&country=SE");
        buf.append("&country=US");
        int maxResults = getMaxResults(searchCriteria.getMaxResults());
        if (maxResults > 0) {
            buf.append("&maxRows="+Integer.toString(maxResults));
        }

        String userName = PropertyUtil.get("search.channel.GEONAMES_CHANNEL.username");
        buf.append("&username="+userName);
        String data = IOHelper.readString(getConnection(buf.toString()));
        log.debug("DATA: " + data);

        return JSONHelper.createJSONObject(data).getJSONArray("geonames");
    }

    /**
     * Returns the channel search results.
     * @param searchCriteria Search criteria.
     * @return Search results.
     */
    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) {
        ChannelSearchResult searchResultList = new ChannelSearchResult();
        
        String srs = searchCriteria.getSRS();
        if( srs == null ) {
        	srs = "EPSG:3067";
        }

        try {
            final JSONArray data = getData(searchCriteria);
            for (int i = 0; i < data.length(); i++) {
                JSONObject dataItem = data.getJSONObject(i);
                SearchResultItem item = new SearchResultItem();
                item.setTitle(JSONHelper.getStringFromJSON(dataItem, "name", ""));
                item.setDescription(JSONHelper.getStringFromJSON(dataItem, "display_name", ""));
                item.setLocationTypeCode(JSONHelper.getStringFromJSON(dataItem, "fcodeName", ""));
                item.setType(JSONHelper.getStringFromJSON(dataItem, "fclName", ""));
                String adminName = "";
                // The most informative adminName available
                for (int j = 5; j > 0; j--) {
                    adminName = JSONHelper.getStringFromJSON(dataItem, "adminName"+Integer.toString(j), "");
                    if (adminName.length() > 0) {
                        break;
                    }
                }
                if (adminName.length() > 0) {
                    adminName = adminName + ", ";
                }
                adminName = adminName + JSONHelper.getStringFromJSON(dataItem, "countryName", "");
                item.setRegion(adminName);
                item.setLon(JSONHelper.getStringFromJSON(dataItem, "lng", ""));
                item.setLat(JSONHelper.getStringFromJSON(dataItem, "lat", ""));
                searchResultList.addItem(item);
                Point p1 = ProjectionHelper.transformPoint(item.getLon(), item.getLat(), "EPSG:4326", srs);
                if (p1 != null) {
                    item.setLon(p1.getLon());
                    item.setLat(p1.getLat());
                }

                log.debug("ITEM: " + item.toString());
            }
        } catch (Exception e) {
            log.error(e, "Failed to search locations from GeoNames Search Webservice");
        }
        return searchResultList;
    }
}
