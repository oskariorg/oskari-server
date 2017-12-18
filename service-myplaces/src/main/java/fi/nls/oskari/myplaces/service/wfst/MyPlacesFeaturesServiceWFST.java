package fi.nls.oskari.myplaces.service.wfst;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.wfst.response.InsertedFeature;
import org.oskari.wfst.response.TransactionResponseParser_100;
import org.oskari.wfst.response.TransactionResponse_100;
import org.xml.sax.SAXException;

import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.myplaces.service.MyPlacesFeaturesService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;

/**
 * WFS-T implementation of MyPlacesFeaturesService
 */
public class MyPlacesFeaturesServiceWFST extends BaseServiceWFST implements MyPlacesFeaturesService {

    @Override
    public JSONObject getFeaturesByCategoryId(long categoryId, String crs)
            throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MyPlacesFeaturesWFSTRequestBuilder.getMyPlacesByCategoryId(baos, crs, categoryId);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            return readFeatureCollection(conn);
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        }
    }

    @Override
    public JSONObject getFeaturesByUserId(String uuid, String crs)
            throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MyPlacesFeaturesWFSTRequestBuilder.getMyPlacesByUserId(baos, crs, uuid);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            return readFeatureCollection(conn);
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        }
    }

    @Override
    public JSONObject getFeaturesByMyPlaceId(long[] ids, String crs)
            throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String[] prefixedIds = MyPlacesFeaturesWFSTRequestBuilder.prefixIds(ids);
            MyPlacesFeaturesWFSTRequestBuilder.getMyPlacesById(baos, crs, prefixedIds);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            return readFeatureCollection(conn);
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        }
    }

    @Override
    public long[] insert(List<MyPlace> places) throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MyPlacesFeaturesWFSTRequestBuilder.insertMyPlaces(baos, places);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            TransactionResponse_100 resp = readTransactionResp(conn);
            if (resp.getStatus() != TransactionResponse_100.Status.SUCCESS) {
                throw new ServiceException("Insert failed, locator:" +
                        resp.getLocator() + " message: " + resp.getMessage());
            }
            List<InsertedFeature> insertedFeatures = resp.getInsertedFeatures();
            return insertedFeatures.stream()
                    .map(InsertedFeature::getFid)
                    .mapToLong(MyPlacesFeaturesWFSTRequestBuilder::removePrefixFromId)
                    .toArray();
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS-T request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        }
    }

    @Override
    public int update(List<MyPlace> places) throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MyPlacesFeaturesWFSTRequestBuilder.updateMyPlaces(baos, places);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            TransactionResponse_100 resp = readTransactionResp(conn);
            if (resp.getStatus() != TransactionResponse_100.Status.SUCCESS) {
                throw new ServiceException("Update failed, locator:" +
                        resp.getLocator() + " message: " + resp.getMessage());
            }
            return places.size();
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS-T request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        }
    }

    @Override
    public int delete(long[] ids) throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MyPlacesFeaturesWFSTRequestBuilder.deleteMyPlaces(baos, ids);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            TransactionResponse_100 resp = readTransactionResp(conn);
            if (resp.getStatus() != TransactionResponse_100.Status.SUCCESS) {
                throw new ServiceException("Delete failed, locator:" +
                        resp.getLocator() + " message: " + resp.getMessage());
            }
            return ids.length;
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS-T request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        }
    }

    private void removePrefixesFromIds(JSONObject featureCollection)
            throws JSONException {
        JSONArray features = featureCollection.getJSONArray("features");
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            String id = feature.getString("id");
            feature.put("id", MyPlacesFeaturesWFSTRequestBuilder.removePrefixFromId(id));
        }
    }

    private JSONObject readFeatureCollection(HttpURLConnection conn)
            throws ServiceException {
        try {
            byte[] resp = IOHelper.readBytes(conn);
            String str = new String(resp, StandardCharsets.UTF_8);
            JSONObject featureCollection = new JSONObject(str);
            removePrefixesFromIds(featureCollection);
            return featureCollection;
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        } catch (JSONException e) {
            throw new ServiceException("Received invalid response from service", e);
        }
    }

    protected TransactionResponse_100 readTransactionResp(HttpURLConnection conn)
            throws ServiceException {
        try {
            byte[] resp = IOHelper.readBytes(conn);
            return TransactionResponseParser_100.parse(resp);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new ServiceException("Failed to parse TransactionResponse", e);
        }
    }

}
