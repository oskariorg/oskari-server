package fi.nls.oskari.control.metadata;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.domain.SelectItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.MetadataCatalogueChannelSearchService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.XmlHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Handler for metadata catalogue search field. Responsible for querying the service for valid options using GetDomain query (#getOptions) and
 * handling parameters from search form to search criteria (#handleParam).
 */
public class MetadataFieldHandler {

    private static final Logger log = LogFactory.getLogger(MetadataFieldHandler.class);

    private final static NodeList EMPTY_NODELIST = new EmptyNodeList();

    private MetadataField field = null;
    private String serverURL = MetadataCatalogueChannelSearchService.getServerURL();
    private String serverPath = MetadataCatalogueChannelSearchService.getServerPath();
    private String queryParams = "?" + PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.queryParams", "SERVICE=CSW&VERSION=2.0.2&request=GetDomain&PropertyName=");
    private Cache<Set<SelectItem>> cache = CacheManager.getCache(MetadataFieldHandler.class.getCanonicalName());

    public String getPropertyName() {
        return getMetadataField().getProperty();
    }

    public void setMetadataField(final MetadataField field) {
        this.field = field;
    }

    public MetadataField getMetadataField() {
        return field;
    }

    public String getSearchURL() {
        return serverURL + serverPath + queryParams;
    }

    public JSONArray getOptions(final String language) {
        JSONArray values = new JSONArray();
        Set<SelectItem> items = getProperties();
        for(SelectItem item : items) {
            final JSONObject value = JSONHelper.createJSONObject("val", item.getValue());
            JSONHelper.putValue(value, "locale", item.getName(true));
            values.put(value);
        }

        return values;
    }

    public JSONArray getOptions(final String language, final String spaceChar) {
        JSONArray values = new JSONArray();
        Set<SelectItem> items = getProperties();
        for(SelectItem item : items) {
            String val = item.getValue();
            val = val.replace(" ", spaceChar);
            final JSONObject value = JSONHelper.createJSONObject("val", val);
            JSONHelper.putValue(value, "locale", item.getName(true));
            values.put(value);
        }

        return values;
    }

    public void handleParam(final String param, final SearchCriteria criteria) {
        if(param == null || param.isEmpty()) {
            // empty param -> skip
            return;
        }
        final MetadataField field = getMetadataField();

        // This is done because of GeoNetwork cannot query GetRecord for special cases. For example: space are not allowed when searching OrganisationName for LocalisedCharacterString.
        final String spaceCharReplace = PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.field."+ field.getName() +".space.char", null);

        if(field.isMulti()) {
            String[] values = param.split("\\s*,\\s*");
            criteria.addParam(getPropertyName(), values);
        }
        else if(spaceCharReplace == null) {
            criteria.addParam(getPropertyName(), param);
        }
        else {
            String replacedParam = param.replace(" ", spaceCharReplace);
            criteria.addParam(getPropertyName(), replacedParam);
        }
    }

    private Set<SelectItem> getProperties() {
        return getProperties(getPropertyName());
    }

    private Set<SelectItem> getProperties(final String propertyName) {
        Set<SelectItem> response = cache.get(propertyName);
        if(response != null) {
            return response;
        }

        response = new TreeSet<SelectItem>();

        final String url = getSearchURL() + propertyName;
        final NodeList valueList = getTags(url, "csw:Value");
        List<String> blacklist = field.getBlacklist();
        for (int i = 0; i < valueList.getLength(); i++) {
            String value = valueList.item(i).getChildNodes().item(0).getTextContent();
            if (blacklist.contains(value)) {
                continue;
            }
            response.add(new SelectItem(null, value));
        }
        cache.put(propertyName, response);
        return response;
    }

    private static NodeList getTags(final String url, final String tagName) {
        DataInputStream dis = null;
        try {
            final HttpURLConnection con = IOHelper.getConnection(url);
            dis = new DataInputStream(IOHelper.debugResponse(con.getInputStream()));
            final DocumentBuilder dBuilder = XmlHelper.newDocumentBuilderFactory().newDocumentBuilder();
            final Document doc = dBuilder.parse(dis);
            doc.getDocumentElement().normalize();
            return doc.getElementsByTagName(tagName);
        } catch (Exception e) {
            log.error("Error parsing tags (", tagName, ") from response at", url, ". Message:", e.getMessage());
        }
        finally {
            IOHelper.close(dis);
        }
        // default to empty NodeList
        return EMPTY_NODELIST;
    }
}
