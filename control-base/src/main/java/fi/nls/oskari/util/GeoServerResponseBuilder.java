package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import net.opengis.ows10.ExceptionReportType;
import net.opengis.wfs.InsertResultsType;
import net.opengis.wfs.InsertedFeatureType;
import net.opengis.wfs.TransactionResponseType;
import net.opengis.wfs.TransactionResultsType;
import net.opengis.wfs.TransactionSummaryType;

import org.apache.commons.io.IOUtils;
import org.eclipse.emf.ecore.EObject;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Parser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;

import net.opengis.wfs.WfsFactory;

import java.io.InputStream;
import java.util.*;

import static org.geotools.GML.Version.GML3;


public class GeoServerResponseBuilder {

    private static final Logger log = LogFactory.getLogger(GeoServerResponseBuilder.class);

    private static final List<String> LAYERS_GET_LIST = Arrays.asList("category_name", "default", "stroke_width",
            "stroke_color", "fill_color", "uuid", "dot_color", "dot_size", "border_width", "border_color",
            "dot_shape", "stroke_linejoin", "fill_pattern", "stroke_linecap", "stroke_dasharray", "border_linejoin",
            "border_dasharray");
    private static final String FID_PREFIX_LAYERS = "categories.";
    private static final String FID_PREFIX_FEATURES = "my_places.";

    //WFS1.1 & JSON/GeoJSON response
    public JSONArray buildLayersGet(String response) throws Exception {
        JSONObject featCollection = new JSONObject(response);
        JSONArray  feats = featCollection.getJSONArray("features");
        JSONObject feat;
        int id;
        //parse id from geoserver fid
        for (int i = 0; i < feats.length(); i++){
            feat = feats.getJSONObject(i);
            id = parseIdFromFid(feat.getString("id"));
            feat.put("id", id);
        }
        return feats;

    }
    //WFS1.1
    public List<Integer> buildLayersInsert(String response) throws Exception {
        //MyPlacesResponse resp = parseTransactionResponse(response);
        return parseTransactionResponse(response).getIdList();
    }

    //WFS1.1
    public int buildLayersUpdate(String response) throws Exception {
        //MyPlacesResponse resp = parseTransactionResponse(response);
        return parseTransactionResponse(response).getUpdated();
    }
    //WFS1.1
    public int buildLayersDelete(String response) throws Exception {
        //MyPlacesResponse resp = parseTransactionResponse(response);
        return parseTransactionResponse(response).getDeleted();
    }
    //WFS1.0 JSON/GeoJSON response
    public JSONArray buildFeaturesGet(String response) throws Exception {
        JSONObject featCollection = new JSONObject(response);
        JSONArray  feats = featCollection.getJSONArray("features");
        JSONObject feat;
        int id;
        //parse id from fid
        for (int i = 0; i < feats.length(); i++){
            feat = feats.getJSONObject(i);
            id = parseIdFromFid(feat.getString("id"));
            feat.put("id", id);
        }
        return feats;
    }
    //WFS1.1
    public List<Integer> buildFeaturesInsert(String response) throws Exception {
        //MyPlacesResponse resp = parseTransactionResponse(response);
        return parseTransactionResponse(response).getIdList();
    }
    //WFS1.1
    public int buildFeaturesUpdate(String response) throws Exception {
        //MyPlacesResponse resp = parseTransactionResponse(response);
        return parseTransactionResponse(response).getUpdated();

    }
    //WFS1.1
    public int buildFeaturesDelete(String response) throws Exception {
        //MyPlacesResponse resp = parseTransactionResponse(response);
        return parseTransactionResponse(response).getDeleted();

    }

    private static SimpleFeatureCollection getFeatureCollection(InputStream inputStream, Version configuration) {
        try {
            GML gml = new GML(configuration);
            return gml.decodeFeatureCollection(inputStream);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Couldn't parse response to feature collection", ex);
        }
    }
    //WFS1.1 & GML
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

    //WFS 1.1.0
    private MyPlacesResponse parseTransactionResponse(String transaction) throws Exception {
        final WFSConfiguration configuration = new org.geotools.wfs.v1_1.WFSConfiguration();
        InputStream inputStream = IOUtils.toInputStream(transaction, "UTF-8");
        Parser parser = new Parser(configuration);
        Object parsedResponse = parser.parse(inputStream);
        MyPlacesResponse response = new MyPlacesResponse();

        if (parsedResponse instanceof TransactionResponseType) {
            TransactionResponseType transactionResponse = (TransactionResponseType) parsedResponse;
            TransactionSummaryType summary = transactionResponse.getTransactionSummary();
            response.setDeleted(summary.getTotalDeleted().intValue());
            response.setInserted(summary.getTotalInserted().intValue());
            response.setUpdated(summary.getTotalUpdated().intValue());
            //TransactionResultsType result = transactionResponse.getTransactionResults();
            InsertResultsType insertResults = transactionResponse.getInsertResults();
            for (int i = 0; i < insertResults.getFeature().size(); ++i) {
                String fid = ((InsertedFeatureType) insertResults.getFeature().get(i)).getFeatureId().get(0).toString();
                response.addId(parseIdFromFid(fid));
            }
        } else if (parsedResponse instanceof ExceptionReportType){
            ExceptionReportType excReport = (ExceptionReportType) parsedResponse;
            for (Object e : excReport.getException()){
                log.warn("e:" + e.toString());
            }
            //TODO throw Exception. check what e contains
        }
        return response;
    }
    private static int parseIdFromFid (String fid){
        String id = "-1";
        if (fid.startsWith(FID_PREFIX_FEATURES)){
            id = fid.substring(FID_PREFIX_FEATURES.length());
        } else if (fid.startsWith(FID_PREFIX_LAYERS)){
            id = fid.substring(FID_PREFIX_LAYERS.length());
        }
        return Integer.parseInt(id);
    }
    class MyPlacesResponse {
        int updated;
        int deleted;
        int inserted;
        List <Integer> idList = new ArrayList<Integer>();

        void setDeleted (int deleted){
            this.deleted = deleted;
        }
        int getDeleted (){
            return deleted;
        }
        void setUpdated (int updated){
            this.updated = updated;
        }
        int getUpdated (){
            return updated;
        }
        void setInserted (int inserted){
            this.inserted = inserted;
        }
        int getInserted (){
            return inserted;
        }
        void setIdList (List<Integer> idList){
            this.idList = idList;
        }
        void addId (int id){
            idList.add(id);
        }
        List<Integer> getIdList (){
            return idList;
        }
    }
}
