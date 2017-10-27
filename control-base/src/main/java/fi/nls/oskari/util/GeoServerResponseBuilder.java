package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import net.opengis.wfs.InsertResultsType;
import net.opengis.wfs.InsertedFeatureType;
import net.opengis.wfs.TransactionResponseType;
import org.apache.commons.io.IOUtils;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Parser;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;

import java.io.InputStream;
import java.util.*;

import static org.geotools.GML.Version.GML3;


public class GeoServerResponseBuilder {

    private static final Logger log = LogFactory.getLogger(GeoServerRequestBuilder.class);

    private static final List<String> LAYERS_GET_LIST = Arrays.asList("category_name", "default", "stroke_width",
            "stroke_color", "fill_color", "uuid", "dot_color", "dot_size", "border_width", "border_color",
            "dot_shape", "stroke_linejoin", "fill_pattern", "stroke_linecap", "stroke_dasharray", "border_linejoin",
            "border_dasharray");

    public JSONObject buildLayersGet(String response) throws Exception {
        return new JSONObject(parseLayersGet(response, LAYERS_GET_LIST));
    }

    public JSONObject buildLayersInsert(String response) throws Exception{
        return new JSONObject(parseLayersTransactionResponse(response));
    }

    public JSONObject buildLayersUpdate(String response) throws Exception{
        return new JSONObject(parseLayersTransactionResponse(response));
    }

    public JSONObject buildLayersDelete(String response) throws Exception{
        return new JSONObject(parseLayersTransactionResponse(response));
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

    private static SimpleFeatureCollection getFeatureCollection(InputStream inputStream, Version configuration) {
        try {
            GML gml = new GML(configuration);
            return gml.decodeFeatureCollection(inputStream);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Couldn't parse response to feature collection", ex);
        }
    }

    public static Map<String, Object> parseLayersGet(String response, List<String> propertyList) throws Exception {
        List featuresList = new ArrayList();

        InputStream inputStream = IOUtils.toInputStream(response, "UTF-8");
        SimpleFeatureCollection fc = getFeatureCollection(inputStream, GML3);
        SimpleFeatureIterator it = fc.features();

        while (it.hasNext()) {
            final SimpleFeature feature = it.next();
            Map featureMap = new HashMap();
            featureMap.put("category_id", feature.getID());
            for (String property : propertyList) {
                featureMap.put(property, feature.getProperty(property).getValue());
            }
            featuresList.add(featureMap);
        }
        Map result = new HashMap();
        result.put("categories", featuresList);
        return result;
    }


    public static Map<String, Object> parseLayersTransactionResponse(String response) throws Exception {

        List fidList = new ArrayList();

        InputStream inputStream = IOUtils.toInputStream(response, "UTF-8");
        WFSConfiguration configuration = new org.geotools.wfs.v1_1.WFSConfiguration();
        Parser parser = new Parser(configuration);
        Object parsedResponse = parser.parse(inputStream);

        if (parsedResponse instanceof TransactionResponseType) {
            TransactionResponseType transactionResponse = (TransactionResponseType) parsedResponse;
            InsertResultsType insertResults = transactionResponse.getInsertResults();
            for (int i = 0; i < insertResults.getFeature().size(); ++i) {
                String fid = ((InsertedFeatureType) insertResults.getFeature().get(i)).getFeatureId().get(0).toString();
                Map fidMap = new HashMap();
                fidMap.put("category_id", fid);
                fidList.add(fidMap);
            }

        }

        Map result = new HashMap();
        result.put("fids", fidList);
        return result;
    }
}