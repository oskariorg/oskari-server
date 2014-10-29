package fi.nls.oskari.search;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.log.Logger;

import java.net.URLEncoder;

import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

@Oskari(OpenStreetMapSearchChannel.ID)
public class OpenStreetMapSearchChannel extends SearchChannel {

    /** logger */
    private Logger log = LogFactory.getLogger(this.getClass());
    private String serviceURL = null;
    public static final String ID = "OPENSTREETMAP_CHANNEL";

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
        int maxResults = searchCriteria.getMaxResults();
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
            final JSONArray data = getData(searchCriteria);
            for (int i = 0; i < data.length(); i++) {
                JSONObject dataItem = data.getJSONObject(i);
                JSONObject address = dataItem.getJSONObject("address");
                SearchResultItem item = new SearchResultItem();
                item.setTitle(JSONHelper.getStringFromJSON(dataItem, "display_name", ""));
                item.setDescription(JSONHelper.getStringFromJSON(dataItem, "display_name", ""));
                item.setLocationTypeCode(JSONHelper.getStringFromJSON(dataItem, "class", ""));
                item.setType(JSONHelper.getStringFromJSON(dataItem, "class", ""));
                item.setVillage(JSONHelper.getStringFromJSON(address, "city", ""));
                item.setLon(JSONHelper.getStringFromJSON(dataItem, "lon", ""));
                item.setLat(JSONHelper.getStringFromJSON(dataItem, "lat", ""));
                // FIXME: add more automation on result rank scaling
                try {
                    item.setRank(100*(int)Math.round(dataItem.getDouble("importance")));
                } catch(JSONException e) {
                    item.setRank(0);
                }
                searchResultList.addItem(item);
                try {
                    CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:4326");
                    CoordinateReferenceSystem targetCrs = CRS.decode(srs);
                    boolean lenient = false;
                    MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, lenient);
                    double srcLon = Double.parseDouble(dataItem.getString("lon"));
                    double srcLat = Double.parseDouble(dataItem.getString("lat"));
                    DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, srcLat, srcLon);
                    DirectPosition2D destDirectPosition2D = new DirectPosition2D();
                    mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);
                    String lon = String.valueOf(destDirectPosition2D.x);
                    String lat = String.valueOf(destDirectPosition2D.y);
                    item.setLon(lon);
                    item.setLat(lat);
                } catch(NoSuchAuthorityCodeException e) {
                    log.error(e, "geotools pox");
                    return null;
                } catch(FactoryException e) {
                    log.error(e, "geotools pox factory");
                    return null;
                } catch(JSONException e) {
                    item.setLon("");
                    item.setLat("");
                    item.setContentURL("");
                }
                log.debug("ITEM: " + item.toString());
            }
        } catch (Exception e) {
            log.error(e, "Failed to search locations from register of OpenStreetMap");
        }
        return searchResultList;
    }
}
