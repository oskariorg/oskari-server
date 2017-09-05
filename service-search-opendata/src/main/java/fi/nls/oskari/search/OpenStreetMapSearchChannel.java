package fi.nls.oskari.search;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.net.URLEncoder;

@Oskari(OpenStreetMapSearchChannel.ID)
public class OpenStreetMapSearchChannel extends SearchChannel {

    /** logger */
    private Logger log = LogFactory.getLogger(this.getClass());
    private String serviceURL = null;
    public static final String ID = "OPENSTREETMAP_CHANNEL";
    public final static String SERVICE_SRS = "EPSG:4326";

    private static final String PROPERTY_SERVICE_URL = "search.channel.OPENSTREETMAP_CHANNEL.service.url";


    @Override
    public void init() {
        super.init();
        serviceURL = PropertyUtil.get(PROPERTY_SERVICE_URL, "http://nominatim.openstreetmap.org/search");
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
        if(serviceURL.indexOf("?") > 0)buf.append("&format=json&addressdetails=1");
        else buf.append("?format=json&addressdetails=1");
        // buf.append("&countrycodes=fi");
        buf.append("&accept-language=");
        buf.append(searchCriteria.getLocale());
        int maxResults = getMaxResults(searchCriteria.getMaxResults());
        if (maxResults > 0) {
            buf.append("&limit="+Integer.toString(maxResults));
        }
        buf.append("&q=");
        buf.append(URLEncoder.encode(searchCriteria.getSearchString(),"UTF-8"));
        String data = IOHelper.readString(getConnection(buf.toString()));
        log.debug("DATA: " + data);
        return JSONHelper.createJSONArray(data);
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
            // Lon,lat  (east coordinate is always first in transformation input and output
            CoordinateReferenceSystem sourceCrs = CRS.decode(SERVICE_SRS, true);
            CoordinateReferenceSystem targetCrs = CRS.decode(srs, true);
            final JSONArray data = getData(searchCriteria);
            for (int i = 0; i < data.length(); i++) {
                JSONObject dataItem = data.getJSONObject(i);
                JSONObject address = dataItem.getJSONObject("address");
                SearchResultItem item = new SearchResultItem();
                item.setTitle(JSONHelper.getStringFromJSON(dataItem, "display_name", ""));
                item.setDescription(JSONHelper.getStringFromJSON(dataItem, "display_name", ""));
                item.setLocationTypeCode(JSONHelper.getStringFromJSON(dataItem, "class", ""));
                item.setType(JSONHelper.getStringFromJSON(dataItem, "class", ""));
                item.setRegion(JSONHelper.getStringFromJSON(address, "city", ""));

                item.setLon(JSONHelper.getStringFromJSON(dataItem, "lon", ""));
                item.setLat(JSONHelper.getStringFromJSON(dataItem, "lat", ""));

                // FIXME: add more automation on result rank scaling
                try {
                    item.setRank(100 * (int) Math.round(dataItem.getDouble("importance")));
                } catch (JSONException e) {
                    item.setRank(0);
                }
                searchResultList.addItem(item);
                // convert to map projection
                final Point point = ProjectionHelper.transformPoint(
                        ConversionHelper.getDouble(item.getLon(), -1),
                        ConversionHelper.getDouble(item.getLat(), -1),
                        sourceCrs,
                        targetCrs);
                if (point == null) {
                    item.setLon("");
                    item.setLat("");
                    continue;
                }

                item.setLon(point.getLon());
                item.setLat(point.getLat());

            }
        } catch (Exception e) {
            log.error(e, "Failed to search locations from register of OpenStreetMap");
        }
        return searchResultList;
    }
}
