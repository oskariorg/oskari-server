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
import fi.nls.oskari.search.channel.ELFGeoLocatorSearchChannel;
import fi.nls.oskari.util.XmlHelper;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.w3c.dom.Document;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ELFGeoLocatorParser {
    private Logger log = LogFactory.getLogger(this.getClass());
    public static final String KEY_NAME = "_name";
    public static final String KEY_TYPE = "_type";
    public static final String KEY_LOCATIONTYPE_TITLE = "locationType_title";
    // Role value is value of SI_LocationType gml:id
    public static final String KEY_LOCATIONTYPE_ROLE = "locationType_role";
    public static final String KEY_PARENT_TITLE = "parent_title";
    public static final String KEY_ADMINISTRATOR = "administrator";
    private Map<String, Double> elfScalesForType = null;
    private Map<String, Integer> elfLocationPriority = null;
    private ELFGeoLocatorSearchChannel channel;
    private ELFGeoLocatorCountries countries = null;

    private String serviceSrs = "EPSG:4258";

    public ELFGeoLocatorParser(ELFGeoLocatorSearchChannel elfchannel) {
        this(null, elfchannel);
    }
    public ELFGeoLocatorParser(final String serviceSrs, ELFGeoLocatorSearchChannel elfchannel) {

        // use provided SRS or default to EPSG:4258
        if(serviceSrs != null) {
            log.debug("Using", serviceSrs, "as native SRS");
            this.serviceSrs = serviceSrs.toUpperCase();
        }
        channel = elfchannel;
        countries = ELFGeoLocatorCountries.getInstance();

        elfScalesForType = channel.getElfScalesForType();
        if(elfScalesForType == null) {
            log.debug("Scale relation to locationtypes is not set ");
        }

        elfLocationPriority = channel.getElfLocationPriority();
        if(elfLocationPriority == null) {
            log.debug("priority relation to locationtypes is not set ");
        }
    }

    /**
     * Parse ELF Geolocator  response to search item list
     *
     * @param data   ELF Geolocator response (fuzzySearch or GetFeature)
     * @param epsg   coordinate ref system of target system (map)
     * @param locale  Locale  current locale
     * @param exonym if true, all alternatives are returned
     * @return
     */
    public ChannelSearchResult parse(String data, String epsg, Locale locale, Boolean exonym) {

        ChannelSearchResult searchResultList = new ChannelSearchResult();

        try {
            //Remove schemalocation for faster parse
            DocumentBuilder db = XmlHelper.newDocumentBuilderFactory().newDocumentBuilder();
            InputStream datain = new ByteArrayInputStream(data.getBytes("UTF-8"));
            Document d = db.parse(datain);
            if(d.getDocumentElement().hasAttribute("xsi:schemaLocation")){
                d.getDocumentElement().removeAttribute("xsi:schemaLocation");
            }

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
            FeatureIterator features = fc.features();

            while (features.hasNext()) {

                SimpleFeature feature = (SimpleFeature) features.next();

                Map<String, Object> result = new HashMap<String, Object>();
                // flat attributes and property values
                this.parseFeatureProperties(result, feature);

                List<String> names = this.findProperties(result, KEY_NAME);
                List<String> types = this.findProperties(result, KEY_TYPE);
                List<String> loctypes = this.findProperties(result, KEY_LOCATIONTYPE_TITLE);
                List<String> loctypeids = this.findProperties(result, KEY_LOCATIONTYPE_ROLE);
                List<String> parents = this.findProperties(result, KEY_PARENT_TITLE);
                List<String> descs = this.findProperties(result, KEY_ADMINISTRATOR);

                String lon = "";
                String lat = "";
                Point lowerLeft = null;
                Point upperRight = null;
                /*
                // The bounds doesn't seem to return anything useful with this service so ignore it.
                // Enable once the results from the service are more promising...
                // ATM the bounds for example "Finland" the country are a point to Helsinki the town...
                if(f.getBounds() != null) {
                    log.debug("Bounds:", f.getBounds(), "min y", f.getBounds().getMinY(), "min x", f.getBounds().getMinX(),
                            "max y", f.getBounds().getMaxY(), "max x", f.getBounds().getMaxX());
                    lowerLeft = ProjectionHelper.transformPoint(f.getBounds().getMinY(), f.getBounds().getMinX(), serviceSrs, epsg); //"EPSG:3067"
                    upperRight = ProjectionHelper.transformPoint(f.getBounds().getMaxY(), f.getBounds().getMaxX(), serviceSrs, epsg); //"EPSG:3067"
                    log.debug("Bounds:", f.getBounds());
                }
                */
                if (feature.getDefaultGeometry() instanceof com.vividsolutions.jts.geom.Point) {
                    com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) feature.getDefaultGeometry();
                    log.debug("Original coordinates - x:", point.getX(), "y:", point.getY());

                    Point p2 = null;
                    p2 = ProjectionHelper.transformPoint(point.getX(), point.getY(), serviceSrs, epsg);

                    log.debug("Transformed coordinates - x:", p2.getLon(), "y:", p2.getLat());
                    if (p2 != null) {
                        lon = "" + p2.getLon();
                        lat = "" + p2.getLat();
                    }
                }

                // Loop names - multiply items, if exomym true
                int size = names.size();
                if (size > 0 && !exonym) size = 1;   // 1st one when exonym false

                SearchResultItem item = new SearchResultItem();

                item.addValue("exonymNames", getVariantName(names, types));
                item.setTitle(getOfficialName(names, types));

                for (int k = 0; k < size; k++) {
                    if (types.size() >= k + 1) {
                        item.setType(types.get(k));
                        item.setLocationTypeCode(types.get(k));
                    }
                }

                if (loctypes.size() > 0) {
                    item.setLocationTypeCode(loctypes.get(0));
                    item.setType(loctypes.get(0));
                }

                item.setRegion("");
                item.setDescription("");

                if (parents.size() > 0){
                    item.setRegion(parents.get(0));
                }else if (descs.size() > 0){
                    item.setRegion(getAdminCountry(locale, descs.get(0)));
                }

                if (descs.size() > 0){
                    item.setDescription(descs.get(0));
                }

                //Zoom scale
                if (loctypeids.size() > 0) {
                    Double scale = this.elfScalesForType.get(loctypeids.get(0));
                    scale = scale != null ? scale : -1d;
                    item.setZoomScale(scale);
                }

                //Priority
                if (loctypeids.size() > 0) {
                    Integer rank = this.elfLocationPriority.get(loctypeids.get(0));
                    rank = rank != null ? rank : 9999;
                    item.setRank(rank);
                }

                item.setLon(lon);
                item.setLat(lat);
                if(lowerLeft != null && upperRight != null) {
                    item.setEastBoundLongitude("" + upperRight.getLon());
                    item.setNorthBoundLatitude("" + upperRight.getLat());
                    item.setWestBoundLongitude("" + lowerLeft.getLon());
                    item.setSouthBoundLatitude("" + lowerLeft.getLat());
                }
                searchResultList.addItem(item);

            }  // Feature loop

        } catch (Exception e) {
            log.error(e, "Failed to search locations from register of ELF GeoLocator");
        }
        return searchResultList;
    }


    private String getOfficialName(List<String> names, List<String> types){
        if(names != null && types != null){
            int index = 0;
            for(String type : types){
                if(type.equals("official"))
                    return names.get(index);
                index++;
            }
        } else{
            log.debug("No names found ");
            return "";
        }
        log.debug("No official name found - use 1st variant ");
        return !names.isEmpty() ? names.get(0) : "";
    }

    private List<String> getVariantName(List<String> names, List<String> types){
        List<String> exonymNames = new  ArrayList<String>();

        if(names != null && types != null){
            int index = 0;
            for(String type : types){
                if(type.equals("variant"))
                    exonymNames.add(names.get(index));

                index++;
            }
        }else{
            log.error("No names found!!!!");
            return null;
        }
        return exonymNames;
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
                    result.put(field, value);
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
        int count = 0;
        for (Map map : valuein) {
            parseFeaturePropertiesMap(result, (Map<String, Object>) map, parentKey + "_" + Integer.toString(count));
            count++;
        }

    }

    private static List<String> findProperties(Map<String, Object> result, String key) {
        List<String> values = new ArrayList<>();
        List<Integer> order = new ArrayList<>();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            Object value = entry.getValue();
            if (value != null) { // hide null properties
                if (value instanceof String) {
                    if (entry.getKey().endsWith(key)) {
                        values.add(value.toString());
                        // Trick order num hack for ordering properties later on because of original hash order
                        String[] num = entry.getKey().split("_");
                        if (num.length > 2){
                            order.add(Integer.parseInt(num[num.length - 2]));
                        }
                    }
                }
            }
        }
        //Sort similiar name properties of flatted data
        List<String> ordervalues = Arrays.asList(new String[values.size()]);
        if(order.size() > 1 && order.size() == values.size()){
            for(int i = 0 ; i < order.size(); i++) {
                try {
                    ordervalues.set(order.get(i).intValue(), values.get(i));
                }
                catch (Exception e){
                    return values;
                }
            }
            return ordervalues;
        }
        return values;
    }

    /**
     * Get ELF geolocator administrator country code
     * @param locale  Locale current locale
     * @param admin_name  administrator name
     * @return
     */
    public String getAdminCountry(Locale locale, String admin_name) {
        return countries.getAdminCountry(locale, admin_name);
    }


    /**
     * Transform point to  CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:4258")
     *
     * @param lon  longitude
     * @param lat  latitude
     * @param epsg source Crs
     * @return
     */
    public String[] transformLonLat(String lon, String lat, String epsg) {
        String[] lonlat = new String[2];

        lonlat[0] = lon;
        lonlat[1] = lat;
        if (epsg.toUpperCase().equals(serviceSrs)) return lonlat;

        try {

            Point p1 = ProjectionHelper.transformPoint(lon, lat, epsg, serviceSrs);
            lonlat[0] = p1.getLonToString();
            lonlat[1] = p1.getLatToString();

            return lonlat;

        } catch (Exception e) {
            log.error(e, "geotools pox");
            return null;
        }

    }
}