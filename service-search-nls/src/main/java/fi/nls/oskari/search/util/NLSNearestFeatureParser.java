package fi.nls.oskari.search.util;

/**
 * Created with IntelliJ IDEA.
 * User: Oskari team
 * Date: 7.5.2014
 * Time: 10:09
 * To change this template use File | Settings | File Templates.
 */

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.XmlHelper;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NLSNearestFeatureParser {
    private Logger log = LogFactory.getLogger(this.getClass());
    public static final String KEY_KUNTANIMIFIN = "kuntanimiFin";
    public static final String KEY_KUNTANIMISWE = "kuntanimiSwe";
    public static final String KEY_KATUNUMERO = "_katunumero";
    public static final String KEY_KATUNIMI = "_katunimi";
    public static final String KEY_KIELI = "_kieli";
    public static final String KEY_SIJAINTI = "sijainti";
    public static final String LANG_SWE = "swe";

    public static final String TAG_OSOITEPISTE = "oso:Osoitepiste";
    public static final String TAG_OSOITE = "oso:Osoite";
    public static final String TAG_KIELI = "oso:kieli";
    public static final String TAG_KATUNIMI= "oso:katunimi";

    public final static String SERVICE_SRS = "EPSG:3067";

    /**
     * Parse NLS nearestfeature  response to search item list
     * https://ws.nls.fi/maasto/nearestfeature
     *
     * @param data  NLS nearest response (nearest feature service)
     * @param epsg  coordinate ref system of target system (map)
     * @param lang3 language code  ISO3
     * @return
     */
    public ChannelSearchResult parse(String data, String epsg, String lang3) {

        ChannelSearchResult searchResultList = new ChannelSearchResult();

        try {
            //Remove schemalocation for faster parse
            DocumentBuilder db = XmlHelper.newDocumentBuilderFactory().newDocumentBuilder();
            InputStream datain = new ByteArrayInputStream(data.getBytes("UTF-8"));
            Document d = db.parse(datain);
            d.getDocumentElement().removeAttribute("xsi:schemaLocation");

            // Back to input stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(d);
            Result outputTarget = new StreamResult(outputStream);
            Transformer transformer = XmlHelper.newTransformerFactory().newTransformer();
            transformer.transform(xmlSource, outputTarget);
            InputStream xml = new ByteArrayInputStream(outputStream.toByteArray());

            //create the parser with the gml 3.0 configuration
            org.geotools.xml.Configuration configuration = new org.geotools.gml3.GMLConfiguration();
            org.geotools.xml.Parser parser = new org.geotools.xml.Parser(configuration);
            parser.setValidating(false);
            parser.setFailOnValidationError(false);
            parser.setStrict(false);



            //parse featurecollection
            // TODO: fix parse error in Geotools
            // Fix osoite elements parsing, both osoite elements have content of 1st osoite element
            //
            FeatureCollection<SimpleFeatureType, SimpleFeature> fc = null;
            try {
                Object obj = parser.parse(xml);
                if (obj instanceof Map) {
                    log.error("parse error");
                    return null;
                } else {
                    fc = (FeatureCollection<SimpleFeatureType, SimpleFeature>) obj;
                }
            } catch (Exception e) {
                log.error(e, "parse error");
                return null;
            }
            FeatureIterator i = fc.features();

            int nfeatures = 0;
            while (i.hasNext()) {
                SimpleFeature f = (SimpleFeature) i.next();

                Map<String, Object> result = new HashMap<String, Object>();
                // flat attributes and property values
                this.parseFeatureProperties(result, f);

                List<String> fin_names = this.findProperties(result, KEY_KUNTANIMIFIN);
                List<String> swe_names = this.findProperties(result, KEY_KUNTANIMISWE);
                List<String> nums = this.findProperties(result, KEY_KATUNUMERO);
                List<String> roads = this.findProperties(result, KEY_KATUNIMI);
                List<String> langs = this.findProperties(result, KEY_KIELI);
                List<String> locations = this.findProperties(result, KEY_SIJAINTI);

                SearchResultItem item = new SearchResultItem();

                item.setType("");
                item.setLocationTypeCode("");

                item.setRegion("");
                item.setDescription("");
                if (fin_names.size() > 0) item.setRegion(fin_names.get(0));
                String address = (nums.size() > 0) ? roads.get(0) + " " + nums.get(0) : roads.get(0);

                if (lang3.equals(LANG_SWE)) {
                    if (swe_names.size() > 0) item.setRegion(swe_names.get(0));
                    // Double..Triple work, because gotools doesn't parse multiple equal name properties
                    String adress = getSweAddress(d, f.getID());
                    if(adress != null)  address = (nums.size() > 0) ? adress + " " + nums.get(0) : adress;
                }
                item.setTitle(address);

                //Locations POINT (385500.99 6675044.547)
                try {
                    String pp[] = locations.get(0).replace("(", "").replace(")", "").split(" ");
                    Point p = ProjectionHelper.transformPoint(pp[1], pp[2], SERVICE_SRS, epsg);


                    item.setLon(p.getLonToString());
                    item.setLat(p.getLatToString());

                    item.setEastBoundLongitude("" + item.getLon());
                    item.setNorthBoundLatitude("" + item.getLat());
                    item.setWestBoundLongitude("" + item.getLon());
                    item.setSouthBoundLatitude("" + item.getLat());

                } catch (Exception e) {

                }


                searchResultList.addItem(item);


            }  // Feature loop

        } catch (Exception e) {
            log.error(e, "Failed to search locations from register of NLS nearest feature");
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
                } else {

                    result.put(field, value.toString());
                }
            }
        }
    }

    /**
     * Parse sub value of property Map
     *
     * @param result    properties and attribute
     * @param valuein   subMap
     * @param parentKey name of sub map property
     */
    private static void parseFeaturePropertiesMap(Map result, Map<String, Object> valuein, String parentKey) {

        for (Map.Entry<String, Object> entry : valuein.entrySet()) {
            Object value = entry.getValue();
            if (value != null) { // hide null properties
                if (value instanceof Map) {
                    parseFeaturePropertiesMap(result, (Map<String, Object>) value, parentKey + "_" + entry.getKey());
                } else if (value instanceof List) {
                    parseFeaturePropertiesMapList(result, (List) value, entry.getKey());
                } else {
                    // Key might be null, use parent field name
                    if (entry.getKey() == null) {
                        result.put(parentKey, value);
                    } else result.put(parentKey + "_" + entry.getKey(), value);
                }
            }

        }

    }

    /**
     * Parse property value(s) when property value is ArrayList<Map>
     *
     * @param result    properties and attributes
     * @param valuein   arraylist of sub maps  (sub property values)
     * @param parentKey field name in case of null entry key
     */
    private static void parseFeaturePropertiesMapList(Map result, List<Map<String, Object>> valuein, String parentKey) {
        int count = 1;
        for (Map map : valuein) {
            parseFeaturePropertiesMap(result, (Map<String, Object>) map, parentKey + Integer.toString(count));
            count++;
        }

    }

    private static List<String> findProperties(Map<String, Object> result, String key) {

        List<String> values = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            Object value = entry.getValue();
            if (value != null) { // hide null properties
                if (value instanceof String) {
                    if (entry.getKey().endsWith(key)) values.add(value.toString());
                }
            }

        }
        return values;

    }

    /**
     * Transform point to  service crs system
     *
     * @param lon  longitude
     * @param lat  latitude
     * @param epsg source Crs
     * @return
     */
    public String transformLonLat(double lon, double lat, String epsg) {

        Point p2 = ProjectionHelper.transformPoint(lon, lat, epsg, SERVICE_SRS);

        if (p2 != null) return p2.getLonToString() + "," + p2.getLatToString() + "," + SERVICE_SRS;

        return null;
    }
    private String getSweAddress(Document d, String id) {
        NodeList osopists = d.getElementsByTagName(TAG_OSOITEPISTE);

        for (int k = 0; k < osopists.getLength(); k++) {
            Element ne  = (Element) osopists.item(k);
            if(id.equals(ne.getAttribute("gml:id"))){
                // Get osoite nodes
                NodeList oso= ne.getElementsByTagName(TAG_OSOITE);
                for (int i = 0; i < oso.getLength(); i++) {
                    Element os  = (Element) oso.item(i);
                    NodeList langu = os.getElementsByTagName(TAG_KIELI);
                    if(langu != null && langu.getLength() > 0 )
                    {
                        if(langu.item(0).getTextContent().equals(LANG_SWE)){
                            NodeList road = os.getElementsByTagName(TAG_KATUNIMI);
                            if(road != null && road.getLength() > 0 ) return road.item(0).getTextContent();
                        }
                    }

                }
            }

        }

        return null;
    }
}
