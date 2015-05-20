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
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.search.channel.ELFGeoLocatorSearchChannel;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.MathTransform;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.*;

public class ELFGeoLocatorParser {
    private Logger log = LogFactory.getLogger(this.getClass());
    public static final String KEY_NAME = "_name";
    public static final String KEY_TYPE = "_type";
    public static final String KEY_LOCATIONTYPE_TITLE = "locationType_title";
    // Role value is value of SI_LocationType gml:id
    public static final String KEY_LOCATIONTYPE_ROLE = "locationType_role";
    public static final String KEY_PARENT_TITLE = "parent_title";
    public static final String KEY_ADMINISTRATOR = "administrator";
    private JSONObject countryMap = null;
    private Map<String, Double> elfScalesForType = null;

    public final static String SERVICE_SRS = "EPSG:4258";

    public ELFGeoLocatorParser() {

        ELFGeoLocatorSearchChannel elfchannel = new ELFGeoLocatorSearchChannel();
        countryMap = elfchannel.getElfCountryMap();
        if(countryMap == null) log.debug("CountryMap is not set ");
        elfScalesForType = elfchannel.getElfScalesForType();
        if(elfScalesForType == null) log.debug("Scale relation to locationtypes is not set ");

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
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream datain = new ByteArrayInputStream(data.getBytes("UTF-8"));
            Document d = db.parse(datain);
            d.getDocumentElement().removeAttribute("xsi:schemaLocation");

            // Back to input stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(d);
            Result outputTarget = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
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
            FeatureIterator i = fc.features();

            int nfeatures = 0;
            while (i.hasNext()) {
                SimpleFeature f = (SimpleFeature) i.next();

                Map<String, Object> result = new HashMap<String, Object>();
                // flat attributes and property values
                this.parseFeatureProperties(result, f);

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
                    lowerLeft = ProjectionHelper.transformPoint(f.getBounds().getMinY(), f.getBounds().getMinX(), SERVICE_SRS, epsg); //"EPSG:3067"
                    upperRight = ProjectionHelper.transformPoint(f.getBounds().getMaxY(), f.getBounds().getMaxX(), SERVICE_SRS, epsg); //"EPSG:3067"
                    log.debug("Bounds:", f.getBounds());
                }
                */
                if (f.getDefaultGeometry() instanceof com.vividsolutions.jts.geom.Point) {
                    com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) f.getDefaultGeometry();
                    log.debug("Original coordinates - x:", point.getX(), "y:", point.getY());
                    // since ProjectionHelper.isFirstAxisNorth(CRS.decode(SERVICE_SRS)) == true -> y first
                    Point p2 = ProjectionHelper.transformPoint(point.getY(), point.getX(), SERVICE_SRS, epsg); //"EPSG:3067"
                    log.debug("Transformed coordinates - x:", p2.getLon(), "y:", p2.getLat());
                    if(p2 != null) {
                        lon = "" + p2.getLon();
                        lat = "" + p2.getLat();
                    }
                }

                // Loop names - multiply items, if exomym true
                int size = names.size();
                if (size > 0 && !exonym) size = 1;   // 1st one when exonym false
                for (int k = 0; k < size; k++) {
                    SearchResultItem item = new SearchResultItem();
                    item.setTitle(names.get(k));

                    if (types.size() >= k + 1) {
                        item.setType(types.get(k));
                        item.setLocationTypeCode(types.get(k));
                    }

                    if (loctypes.size() > 0) {
                        item.setLocationTypeCode(loctypes.get(0));
                        item.setType(loctypes.get(0));
                    }

                    item.setVillage("");
                    item.setDescription("");

                    if (parents.size() > 0) item.setVillage(parents.get(0));
                    else if (descs.size() > 0) item.setVillage(getAdminCountry(locale, descs.get(0)));

                    if (descs.size() > 0) item.setDescription(descs.get(0));

                    //Zoom scale
                    if (loctypeids.size() > 0) {
                       Double scale = this.elfScalesForType.get(loctypeids.get(0));
                        scale = scale != null ? scale : -1d;
                        item.setZoomScale(scale);
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
                }

            }  // Feature loop

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
     * Get ELF geolocator administrator name(s) of country based
     *
     * @param country_code ISO Country code 2 ch
     * @return
     */
    public String[] getAdminName(String country_code) {
        String[] value = {""};

        try {

            if (this.countryMap.has(country_code)) {
                value = countryMap.getString(country_code).split(";");
            }

        } catch (Exception e) {
            log.debug("Failed to get ELF country codes to " + country_code);

        }


        return value;

    }

    /**
     * Get ELF geolocator administrator country code
     * @param locale  Locale current locale
     * @param admin_name  administrator name
     * @return
     */
    public String getAdminCountry(Locale locale, String admin_name) {
        String country = "";

        try {

            Iterator<?> keys = countryMap.keys();

            while( keys.hasNext() ){
                String key = (String)keys.next();
                String[] admins = countryMap.get(key).toString().split(";");
                for (String s: admins)
                {
                   if(s.equals(admin_name)) {
                       Locale obj = new Locale("", key);
                       return obj.getDisplayCountry(locale);
                   }
                }

                }

        } catch (JSONException e) {
            log.debug("Failed to get ELF country name to " + admin_name);

        }


        return country;

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
        if (epsg.toUpperCase().equals("EPSG:4258")) return lonlat;

        try {

            CoordinateReferenceSystem sourceCrs = CRS.decode(epsg);
            CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:4258");


            MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, true);

            DirectPosition2D srcDirectPosition2D = null;

            DirectPosition2D destDirectPosition2D = new DirectPosition2D();

            // Switch direction, if 1st coord is to the north
            if ( sourceCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.NORTH ||
                    sourceCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.UP ||
                    sourceCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.DISPLAY_UP) {
                srcDirectPosition2D = new DirectPosition2D(sourceCrs, Double.valueOf(lat), Double.valueOf(lon));
            } else {
                srcDirectPosition2D = new DirectPosition2D(sourceCrs, Double.valueOf(lon), Double.valueOf(lat));
            }

            mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);

            lonlat[0] = String.valueOf(destDirectPosition2D.x);
            lonlat[1] = String.valueOf(destDirectPosition2D.y);
            // Switch direction, if 1st coord is to the north
            if (targetCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.NORTH ||
                    targetCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.UP ||
                    targetCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.DISPLAY_UP) {
                lonlat[0] = String.valueOf(destDirectPosition2D.y);
                lonlat[1] = String.valueOf(destDirectPosition2D.x);
            }
            return lonlat;

        } catch (Exception e) {
            log.error(e, "geotools pox");
            return null;
        }

    }
}
