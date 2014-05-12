package fi.nls.oskari.search.util;

/**
 * Created with IntelliJ IDEA.
 * User: Oskari team
 * Date: 7.5.2014
 * Time: 10:09
 * To change this template use File | Settings | File Templates.
 */

import com.vividsolutions.jts.geom.Point;
import fi.mml.portti.service.search.ChannelSearchResult;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ELFGeoLocatorParser {
    private Logger log = LogFactory.getLogger(this.getClass());
    public static final String KEY_NAME = "_name";
    public static final String KEY_TYPE = "_type";
    public static final String KEY_LOCATIONTYPE_TITLE = "locationType_title";
    public static final String KEY_PARENT_TITLE = "parent_title";
    public static final String KEY_ADMINISTRATOR = "administrator";

    /**
     * Parse ELF Geolocator  response to search item list
     *
     * @param data   ELF Geolocator response (fuzzySearch or GetFeature)
     * @param epsg   coordinate ref system of target system (map)
     * @param exonym if true, all alternatives are returned
     * @return
     */
    public ChannelSearchResult parse(String data, String epsg, Boolean exonym) {

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

            Object obj = null;
            FeatureCollection<SimpleFeatureType, SimpleFeature> fc = null;
            try {
                obj = parser.parse(xml);
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
                List<String> parents = this.findProperties(result, KEY_PARENT_TITLE);
                List<String> descs = this.findProperties(result, KEY_ADMINISTRATOR);


                String lon = "";
                String lat = "";


                try {
                    Point point = null;

                    if (f.getDefaultGeometry() instanceof Point) {
                        point = (Point) f.getDefaultGeometry();
                    }
                    if (point != null) {
                        CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:4258");
                        CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:3067");
                        if (epsg != null) targetCrs = CRS.decode(epsg);

                        boolean lenient = false;
                        MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, lenient);

                        DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, point.getY(), point.getX());
                        DirectPosition2D destDirectPosition2D = new DirectPosition2D();
                        mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);
                        lon = String.valueOf(destDirectPosition2D.x);
                        lat = String.valueOf(destDirectPosition2D.y);
                        // Switch direction, if 1st coord is to the north
                        if (targetCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.NORTH ||
                                targetCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.UP ||
                                targetCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.DISPLAY_UP) {
                            lon = String.valueOf(destDirectPosition2D.y);
                            lat = String.valueOf(destDirectPosition2D.x);
                        }

                    }


                } catch (NoSuchAuthorityCodeException e) {
                    log.error(e, "geotools pox");
                    return null;
                } catch (FactoryException e) {
                    log.error(e, "geotools pox factory");
                    return null;
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

                    if (descs.size() > 0) item.setDescription(descs.get(0));

                    item.setLon(lon);
                    item.setLat(lat);
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
