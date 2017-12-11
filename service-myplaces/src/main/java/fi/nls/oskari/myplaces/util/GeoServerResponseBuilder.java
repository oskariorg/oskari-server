package fi.nls.oskari.myplaces.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import net.opengis.ows10.ExceptionReportType;
import net.opengis.wfs.InsertResultsType;
import net.opengis.wfs.InsertedFeatureType;
import net.opengis.wfs.TransactionResponseType;
import net.opengis.wfs.TransactionSummaryType;

import org.apache.commons.io.IOUtils;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Parser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import static org.geotools.GML.Version.GML3;

public class GeoServerResponseBuilder {

    private static final Logger log = LogFactory.getLogger(GeoServerResponseBuilder.class);

    private static final String FID_PREFIX_LAYERS = "categories.";
    private static final String FID_PREFIX_FEATURES = "my_places.";

    //WFS1.1 & JSON/GeoJSON response
    public JSONArray buildLayersGet(String response) throws Exception {
        JSONObject featCollection = new JSONObject(response);
        JSONArray  feats = featCollection.getJSONArray("features");
        JSONObject feat;
        long id;
        //parse id from geoserver fid
        for (int i = 0; i < feats.length(); i++){
            feat = feats.getJSONObject(i);
            id = parseIdFromFid(feat.getString("id"));
            feat.put("id", id);
        }
        return feats;

    }

    //WFS1.0 JSON/GeoJSON response
    public JSONArray buildFeaturesGet(String response) throws JSONException {
        JSONObject featCollection = new JSONObject(response);
        JSONArray  feats = featCollection.getJSONArray("features");
        JSONObject feat;
        long id;
        //parse id from fid
        for (int i = 0; i < feats.length(); i++){
            feat = feats.getJSONObject(i);
            id = parseIdFromFid(feat.getString("id"));
            feat.put("id", id);
        }
        return feats;
    }

    public long[] getInsertedIds(String response) throws IllegalArgumentException {
        return parseTransactionResponse(response)
                .orElseThrow(() -> new IllegalArgumentException())
                .insertedIds;
    }

    public int getTotalUpdated(String response) throws IllegalArgumentException {
        return parseTransactionResponse(response)
                .orElseThrow(() -> new IllegalArgumentException())
                .updated;
    }

    public int getTotalDeleted(String response) throws IllegalArgumentException {
        return parseTransactionResponse(response)
                .orElseThrow(() -> new IllegalArgumentException())
                .deleted;
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
        // TODO: Fix raw types, perhaps use a POJO?
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
    private Optional<MyPlacesResponse> parseTransactionResponse(String transaction) {
        final WFSConfiguration configuration = new org.geotools.wfs.v1_1.WFSConfiguration();
        try (InputStream inputStream = IOUtils.toInputStream(transaction, "UTF-8")) {
            Parser parser = new Parser(configuration);
            Object parsedResponse = parser.parse(inputStream);

            if (parsedResponse instanceof TransactionResponseType) {
                TransactionResponseType transactionResponse = (TransactionResponseType) parsedResponse;
                TransactionSummaryType summary = transactionResponse.getTransactionSummary();

                int updated = summary.getTotalUpdated().intValue();
                int deleted = summary.getTotalDeleted().intValue();
                int inserted = summary.getTotalInserted().intValue();
                long[] insertedIds = new long[inserted];
                InsertResultsType insertResults = transactionResponse.getInsertResults();
                for (int i = 0; i < insertResults.getFeature().size(); ++i) {
                    String fid = ((InsertedFeatureType) insertResults.getFeature().get(i)).getFeatureId().get(0).toString();
                    insertedIds[i] = parseIdFromFid(fid);
                }

                return Optional.of(new MyPlacesResponse(updated, deleted, insertedIds));
            } else if (parsedResponse instanceof ExceptionReportType) {
                ExceptionReportType excReport = (ExceptionReportType) parsedResponse;
                for (Object e : excReport.getException()){
                    log.warn("e:" + e.toString());
                }
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            log.warn(e);
        }
        return Optional.empty();
    }

    private static long parseIdFromFid(String fid) {
        String id;
        if (fid.startsWith(FID_PREFIX_FEATURES)){
            id = fid.substring(FID_PREFIX_FEATURES.length());
        } else if (fid.startsWith(FID_PREFIX_LAYERS)){
            id = fid.substring(FID_PREFIX_LAYERS.length());
        } else {
            id = "-1";
        }
        return Long.parseLong(id);
    }

    private class MyPlacesResponse {

        private final int updated;
        private final int deleted;
        private final long[] insertedIds;

        private MyPlacesResponse(int updated, int deleted, long[] insertedIds) {
            this.updated = updated;
            this.deleted = deleted;
            this.insertedIds = insertedIds;
        }

    }
}
