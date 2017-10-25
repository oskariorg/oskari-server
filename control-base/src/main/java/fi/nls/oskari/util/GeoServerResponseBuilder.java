package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.geotools.GML;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.json.JSONObject;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

import static java.util.Arrays.asList;
import static org.geotools.GML.Version.GML3;


public class GeoServerResponseBuilder {

    private static final Logger log = LogFactory.getLogger(GeoServerRequestBuilder.class);

    private static final List<String> LAYERS_GET_LIST = Arrays.asList("category_name", "default", "stroke_width",
            "stroke_color", "fill_color", "uuid", "dot_color", "dot_size", "border_width", "border_color",
            "dot_shape", "stroke_linejoin", "fill_pattern", "stroke_linecap", "stroke_dasharray", "border_linejoin",
            "border_dasharray");

    public JSONObject buildLayersGet(String response) throws Exception {
        return new JSONObject(parse(response, LAYERS_GET_LIST));
    }

    public JSONObject buildLayersInsert(String response) {
        return null;
    }

    public JSONObject buildLayersUpdate(String response) {
        return null;
    }

    public JSONObject buildLayersDelete(String response) {
        return null;
    }

    public JSONObject buildFeaturesGet(String response) {
        return null;
    }

    public JSONObject buildFeaturesInsert(String response) {
        return null;
    }

    public JSONObject buildFeaturesUpdate(String response) {
        return null;
    }

    public JSONObject buildFeaturesDelete(String response) {
        return null;
    }

    private static SimpleFeatureCollection getFeatureCollection(InputStream inputStream) {
        try {
            GML gml = new GML(GML3);
            return gml.decodeFeatureCollection(inputStream);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Couldn't parse response to feature collection", ex);
        }
    }

    public static Map<String, String> parse(String response, List<String> propertyList) throws ServiceException {
        Map map = new HashMap();
        try {
            InputStream inputStream = IOUtils.toInputStream(response, "UTF-8");
            SimpleFeatureCollection fc = getFeatureCollection(inputStream);
            SimpleFeatureIterator it = fc.features();

            while (it.hasNext()) {
                for (String property : propertyList) {
                    final SimpleFeature feature = it.next();
                    map.put(property, feature.getProperty(property));
                }
            }
        }
        catch (Exception e) {

        }
        return map;
    }
}