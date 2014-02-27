package fi.nls.oskari.search.channel;
/**
 * Search channel for ELF platform
 */

import com.vividsolutions.jts.geom.Point;
import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;

import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.MathTransform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

public class ELFGeoLocatorSearchChannel implements SearchableChannel {

    /** logger */
    private Logger log = LogFactory.getLogger(this.getClass());
    private String serviceURL = null;
    private String serviceRequest = null;
    public static final String ID = "ELFGEOLOCATOR_CHANNEL";
    public static final String PROPERTY_SERVICE_URL = "service.url";
    public static final String PROPERTY_SERVICE_REQUEST = "service.request";
    public static final String KEY_PLACE_HOLDER ="_PLACE_HOLDER_";
    public static final String RESPONSE_CLEAN = "<?xml version='1.0' encoding='UTF-8'?>";
    public static final String REQUEST_FUZZY = "FuzzyNameSearch";
    public static final String REQUEST_GETFEATURE = "GetFeature";
    public static final String REQUEST_FUZZY_TEMPLATE = "?SERVICE=WFS&REQUEST=FuzzyNameSearch&NAME=";
    public static final String REQUEST_GETFEATURE_TEMPLATE = "?SERVICE=WFS&REQUEST=GetFeature&NAMESPACE=xmlns%28iso19112=http://www.isotc211.org/19112%29&TYPENAME=SI_LocationInstance&Version=1.1.0&MAXFEATURES=10&language=eng&FILTER=%3Cogc:Filter%20xmlns:ogc=%22http://www.opengis.net/ogc%22%20xmlns:iso19112=%22http://www.isotc211.org/19112%22%3E%3Cogc:PropertyIsEqualTo%3E%3Cogc:PropertyName%3Eiso19112:alternativeGeographicIdentifiers/iso19112:alternativeGeographicIdentifier/iso19112:name%3C/ogc:PropertyName%3E%3Cogc:Literal%3E_PLACE_HOLDER_%3C/ogc:Literal%3E%3C/ogc:PropertyIsEqualTo%3E%3C/ogc:Filter%3E";


    public void setProperty(String propertyName, String propertyValue) {
        if (PROPERTY_SERVICE_URL.equals(propertyName)) {
            serviceURL = propertyValue;
            log.debug("ServiceURL set to " + serviceURL);
        } else  if (PROPERTY_SERVICE_REQUEST.equals(propertyName)) {
            serviceRequest = propertyValue;
            log.debug("ServiceRequest set to " + serviceRequest);
        } else {
            log.warn("Unknown property for " + ID + " search channel: " + propertyName);
        }
    }

    public String getId() {
        return ID;
    }

    /**
     * Returns the search raw results. 
     * @param searchCriteria Search criteria.
     * @return Result data in JSON format.
     * @throws Exception
     */
    private String getData(SearchCriteria searchCriteria) throws Exception {
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key",PROPERTY_SERVICE_URL);
            return null;
        }
        StringBuffer buf = new StringBuffer(serviceURL);
        if(serviceRequest.equals(REQUEST_FUZZY))
        {
          buf.append(REQUEST_FUZZY_TEMPLATE);
          buf.append(URLEncoder.encode(searchCriteria.getSearchString(),"UTF-8"));
        }
        else if (serviceRequest.equals(REQUEST_GETFEATURE))
        {
            buf.append(REQUEST_GETFEATURE_TEMPLATE.replace(KEY_PLACE_HOLDER, URLEncoder.encode(searchCriteria.getSearchString1stUp(), "UTF-8")));
        }
        else {
            log.warn("ServiceRequest not valid. Add valid property with key",PROPERTY_SERVICE_REQUEST);
            return null;
        }
        return IOHelper.getURL(buf.toString());

    }

    /**
     * Returns the channel search results.
     * @param searchCriteria Search criteria.
     * @return Search results.
     */
    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) {
        ChannelSearchResult searchResultList = new ChannelSearchResult();

        try {
             String data = getData(searchCriteria);

            // Clean xml version for geotools parser
            data = data.replace(RESPONSE_CLEAN,"");

            //Remove schemalocation for faster parse
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream datain = new ByteArrayInputStream(data.getBytes("UTF-8"));
            Document d = db.parse( datain );
            d.getDocumentElement().removeAttribute("xsi:schemaLocation");

            // Back to input stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(d);
            Result outputTarget = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
            InputStream xml = new ByteArrayInputStream(outputStream.toByteArray());

            //create the parser with the gml 3.0 configuration
            org.geotools.xml.Configuration configuration = new org.geotools.gml3.GMLConfiguration();
            org.geotools.xml.Parser parser = new org.geotools.xml.Parser( configuration );
            parser.setValidating(false);
            parser.setFailOnValidationError(false);
            parser.setStrict(false);


            //parse featurecollection

            Object obj = null;
            FeatureCollection<SimpleFeatureType, SimpleFeature> fc = null;
            try {
                obj = parser.parse(xml);
                if(obj instanceof Map) {
                    log.error("parse error");
                    return null;
                }
                else {
                    fc = (FeatureCollection<SimpleFeatureType, SimpleFeature>) obj;
                }
            } catch (Exception e) {
                log.error(e, "parse error");
                return null;
            }
             FeatureIterator i = fc.features();

            int nfeatures = 0;
            while( i.hasNext() ) {
                SimpleFeature f = (SimpleFeature) i.next();
                SearchResultItem item = new SearchResultItem();

                Map<String, Object> result = new HashMap<String, Object>();
                // flat attributes and property values
                this.parseFeatureProperties(result, f);

                if(result.containsKey("alternativeGeographicIdentifiers_name") ) item.setTitle(result.get("alternativeGeographicIdentifiers_name").toString());
                if(result.containsKey("alternativeGeographicIdentifier1_name") ) item.setTitle(result.get("alternativeGeographicIdentifier1_name").toString());
                if(result.containsKey("alternativeGeographicIdentifiers_type") ) item.setType(result.get("alternativeGeographicIdentifiers_type").toString());
                if(result.containsKey("alternativeGeographicIdentifier1_type") ) item.setType(result.get("alternativeGeographicIdentifier1_type").toString());
                if(result.containsKey("alternativeGeographicIdentifiers_type") ) item.setLocationTypeCode(result.get("alternativeGeographicIdentifiers_type").toString());
                if(result.containsKey("alternativeGeographicIdentifier1_type") ) item.setLocationTypeCode(result.get("alternativeGeographicIdentifier1_type").toString());
                if(result.containsKey("locationType_title") ) item.setLocationTypeCode(result.get("locationType_title").toString());

                if(result.containsKey("parent_title") ) item.setVillage(result.get("parent_title").toString());
                if(result.containsKey("administrator") ) item.setDescription(result.get("administrator").toString());

                item.setLon("");
                item.setLat("");
                searchResultList.addItem(item);
                try {
                Point point = null;

                if( f.getDefaultGeometry() instanceof Point) {
                  point = (Point) f.getDefaultGeometry();
                }
                  if(point != null)
                  {
                    String epsg = searchCriteria.getSRS();
                    CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:4258");
                    CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:3067");
                    if(epsg != null) targetCrs = CRS.decode(epsg);

                    boolean lenient = false;
                    MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, lenient);

                      DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, point.getY(), point.getX());
                      DirectPosition2D destDirectPosition2D = new DirectPosition2D();
                      mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);
                      String lon = String.valueOf(destDirectPosition2D.x);
                      String lat = String.valueOf(destDirectPosition2D.y);
                      // Switch direction, if 1st coord is to the north
                      if (targetCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.NORTH ||
                              targetCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.UP ||
                              targetCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.DISPLAY_UP) {
                          lon = String.valueOf(destDirectPosition2D.y);
                          lat = String.valueOf(destDirectPosition2D.x);
                      }
                      item.setLon(lon);
                      item.setLat(lat);
                  }
                } catch(NoSuchAuthorityCodeException e) {
                    log.error(e, "geotools pox");
                    return null;
                } catch(FactoryException e) {
                    log.error(e, "geotools pox factory");
                    return null;
                }
            }

        } catch (Exception e) {
            log.error(e, "Failed to search locations from register of ELF GeoLocator");
        }
        return searchResultList;
    }
    /**
     * Parses SimpleFeature typed object recursively to Map
     * - field names (keys) are combined with parent sub property names, if properties in property
     *
     * @param feature
     * @return feature all properties as a HashMap
     */
    private static void parseFeatureProperties(Map result, SimpleFeature feature) {

        for (Property prop : feature.getProperties()) {
            String field = prop.getName().toString();
            Object value = feature.getAttribute(field);
            if (value != null) { // hide null properties
                if (value instanceof Map) {
                    parseFeaturePropertiesMap(result, (Map) value, field);
                } else if (value instanceof List) {
                    parseFeaturePropertiesMapList(result, (List) value, field);
                } else  {

                    result.put(field, value);
                }
            }
        }

    }

    /**
     * Parse sub value of property Map
     * @param result   properties and attribute
     * @param valuein subMap
     * @param parentKey name of sub map property
     */
    private static void parseFeaturePropertiesMap(Map result, Map<String,Object> valuein, String parentKey) {

        for (Map.Entry<String, Object> entry : valuein.entrySet()) {
             Object value = entry.getValue();
            if (value != null) { // hide null properties
                if (value instanceof Map) {
                    parseFeaturePropertiesMap(result, (Map<String,Object>) value, parentKey+"_"+entry.getKey());
                } else if (value instanceof List) {
                    parseFeaturePropertiesMapList(result, (List) value, entry.getKey());
                } else {
                    // Key might be null, use parent field name
                    if(entry.getKey() == null)
                    {
                        result.put(parentKey, value);
                    }
                    else result.put(parentKey+"_"+entry.getKey(), value);
                }
            }

        }

    }

    /**
     *  Parse property value(s) when property value is ArrayList<Map>
     * @param result  properties and attributes
     * @param valuein arraylist of sub maps  (sub property values)
     * @param parentKey  field name in case of null entry key
     */
    private static void parseFeaturePropertiesMapList(Map result, List<Map<String, Object>> valuein, String parentKey) {
        int count = 1;
        for (Map map : valuein) {
            parseFeaturePropertiesMap(result, (Map<String, Object>) map, parentKey + Integer.toString(count));
            count++;
        }

    }
}
