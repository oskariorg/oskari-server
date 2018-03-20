package fi.nls.oskari.control.feature;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.XmlHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

@OskariActionRoute("InsertFeature")
public class InsertFeatureHandler extends ActionHandler {
    private final static Logger LOG = LogFactory.getLogger(InsertFeatureHandler.class);

    private OskariLayerService layerService;
    private PermissionsService permissionsService;
    private WFSLayerConfigurationService layerConfigurationService;

    @Override
    public void init() {
        super.init();
        layerService = new OskariLayerServiceIbatisImpl();
        permissionsService = new PermissionsServiceIbatisImpl();
        layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();
    }

    @Override
    public void handleAction(ActionParameters params)
            throws ActionException {
        final JSONObject jsonPayload = getJSONPayload(params);
        // throws denied exception if user doesn't have permission to edit the layer
        final OskariLayer layer = getLayerForEditing(jsonPayload.optString("layerId"), params.getUser());
        clearLayerTileCache(layer.getId());
        final String wfstMessage = createWFSTMessage(jsonPayload, layer);
        LOG.debug("Inserting feature to service at", layer.getUrl(), "with payload", wfstMessage);
        final String responseString = postWFSTMessage(layer, wfstMessage);
        final String updatedFeatureId = parseFeatureIdFromResponse(responseString);
        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("fid", updatedFeatureId));
    }

    private JSONObject getJSONPayload(ActionParameters params)
            throws ActionException {

        String featureData = params.getHttpParam("featureData");
        JSONObject jsonObject = JSONHelper.createJSONObject(featureData);
        if (jsonObject == null) {
            throw new ActionParamsException("Couldn't parse featureData JSON from request");
        }
        return jsonObject;
    }

    private OskariLayer getLayerForEditing(String layerId, User user)
            throws ActionException {
        OskariLayer layer = layerService.find(layerId);
        final Resource resource = permissionsService.findResource(new OskariLayerResource(layer));
        final boolean hasPermission = resource.hasPermission(user, Permissions.PERMISSION_TYPE_EDIT_LAYER_CONTENT);
        if (!hasPermission) {
            throw new ActionDeniedException("Can't insert feature");
        }
        return layer;
    }

    private void clearLayerTileCache(int layerId) {
        Set<String> keys = JedisManager.keys("WFSImage_" + Integer.toString(layerId));
        JedisManager.del(keys.toArray(new String[0]));
    }

    private String postWFSTMessage(OskariLayer layer, String payload)
            throws ActionException {
        HttpClient httpClient = getHttpClientForLayer(layer);
        HttpPost request = new HttpPost(layer.getUrl());
        request.addHeader(IOHelper.HEADER_CONTENTTYPE, IOHelper.CONTENT_TYPE_XML);
        request.setEntity(new StringEntity(payload, "UTF-8"));
        try {

            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            if (responseString == null) {
                throw new ActionParamsException("Didn't get any response from service");
            }
            return responseString;
        } catch (IOException ex) {
            throw new ActionParamsException("Error posting the WFS-T message to service");
        }
    }

    private HttpClient getHttpClientForLayer(OskariLayer layer) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        Credentials credentials = new UsernamePasswordCredentials(layer.getUsername(), layer.getPassword());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, credentials);

        httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
        return httpClientBuilder.build();
    }

    private String createWFSTMessage(JSONObject jsonPayload, OskariLayer layer)
            throws ActionException {
        final String srsName = JSONHelper.getStringFromJSON(jsonPayload, "srsName", "http://www.opengis.net/gml/srs/epsg.xml#3067");
        final WFSLayerConfiguration lc = layerConfigurationService.findConfiguration(layer.getId());
        final StringBuilder requestData = new StringBuilder("<wfs:Transaction service='WFS' version='1.1.0' xmlns:ogc='http://www.opengis.net/ogc' xmlns:wfs='http://www.opengis.net/wfs'><wfs:Insert><" + layer.getName() + " xmlns:" + lc.getFeatureNamespace() + "='" + lc.getFeatureNamespaceURI() + "'>");
        try {
            final JSONArray jsonArray = jsonPayload.getJSONArray("featureFields");
            for (int i = 0; i < jsonArray.length(); i++) {
                String key = jsonArray.getJSONObject(i).getString("key");
                String value = jsonArray.getJSONObject(i).getString("value");
                if (value.isEmpty() == false) {
                    requestData.append("<" + key + ">" + value + "</" + key + ">");
                }
            }

            if (jsonPayload.has("geometries")) {
                FillGeometries(lc.getGMLGeometryProperty(), requestData, jsonPayload.getJSONObject("geometries"), srsName);
            }
        } catch (JSONException ex) {
            throw new ActionParamsException("Couldn't create WFS-T message from params", jsonPayload.toString(), ex);
        }
        requestData.append("</" + layer.getName() + "></wfs:Insert></wfs:Transaction>");
        return requestData.toString();
    }

    private String parseFeatureIdFromResponse(String response)
            throws ActionException {

        if (response == null || response.indexOf("Exception") > -1) {
            LOG.error("Exception from WFS-T insert operation", response);
            throw new ActionParamsException("WFS-T operation failed");
        }

        if (response.indexOf("<wfs:totalInserted>1</wfs:totalInserted>") == -1) {
            throw new ActionParamsException("Didn't get the expected response from service", response);
        }

        try {
            DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();

            ByteArrayInputStream input = new ByteArrayInputStream(response.getBytes("UTF-8"));
            Document doc = builder.parse(input);

            NodeList res = doc.getElementsByTagName("ogc:FeatureId");
            Element res3 = (Element) res.item(0);
            return res3.getAttribute("fid");
        } catch (ClientProtocolException ex) {
            LOG.error(ex, "Geoserver connection error");
            throw new ActionException("Geoserver connection error", ex);
        } catch (ParserConfigurationException ex) {
            LOG.error(ex, "Parser configuration error");
            throw new ActionException("Parser configuration error", ex);
        } catch (IOException ex) {
            LOG.error(ex, "IO error");
            throw new ActionException("IO error", ex);
        } catch (SAXException ex) {
            LOG.error(ex, "SAX processing error");
            throw new ActionException("SAX processing error", ex);
        }
    }

    private void FillGeometries(String geometryProperty, StringBuilder requestData, JSONObject geometries,
                                String srsName)
            throws JSONException {
        String geometryType = geometries.getString("type");
        if (geometryType.equals("multipoint")) {
            FillMultiPointGeometries(geometryProperty, requestData, geometries, srsName);
        } else if (geometryType.equals("multilinestring")) {
            FillLineStringGeometries(geometryProperty, requestData, geometries, srsName);
        } else if (geometryType.equals("multipolygon")) {
            FillPolygonGeometries(geometryProperty, requestData, geometries, srsName);
        }
    }

    private void FillMultiPointGeometries(String geometryProperty, StringBuilder requestData, JSONObject geometries,
                                          String srsName)
            throws JSONException {
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<" + geometryProperty
                + "><gml:MultiPoint xmlns:gml='http://www.opengis.net/gml' srsName='" + srsName + "'>");

        for (int i = 0; i < data.length(); i++) {
            requestData.append("<gml:pointMember><gml:Point><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">"
                    + data.getJSONObject(i).getString("x") + "," + data.getJSONObject(i).getString("y")
                    + "</gml:coordinates></gml:Point></gml:pointMember>");
        }
        requestData.append("</gml:MultiPoint></" + geometryProperty + ">");
    }

    private void FillLineStringGeometries(String geometryProperty, StringBuilder requestData, JSONObject geometries,
                                          String srsName)
            throws JSONException {
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<" + geometryProperty
                + "><gml:MultiLineString xmlns:gml='http://www.opengis.net/gml' srsName='" + srsName + "'>");

        for (int i = 0; i < data.length(); i++) {
            requestData
                    .append("<gml:lineStringMember><gml:LineString><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">");
            JSONArray arr = data.getJSONArray(i);
            for (int j = 0; j < arr.length(); j++) {
                requestData.append(arr.getJSONObject(j).getString("x") + "," + arr.getJSONObject(j).getString("y"));
                if (j < (arr.length() - 1)) {
                    requestData.append(" ");
                }
            }
            requestData.append("</gml:coordinates></gml:LineString></gml:lineStringMember>");
        }
        requestData.append("</gml:MultiLineString></" + geometryProperty + ">");
    }

    private void FillPolygonGeometries(String geometryProperty, StringBuilder requestData, JSONObject geometries,
                                       String srsName)
            throws JSONException {
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<" + geometryProperty
                + "><gml:MultiPolygon xmlns:gml='http://www.opengis.net/gml' srsName='" + srsName + "'>");

        for (int i = 0; i < data.length(); i++) {
            requestData.append("<gml:polygonMember><gml:Polygon>");
            JSONArray arr = data.getJSONArray(i);
            for (int j = 0; j < arr.length(); j++) {
                if (j > 0) {
                    requestData.append("<gml:interior><gml:LinearRing><gml:posList>");
                } else {
                    requestData.append("<gml:exterior><gml:LinearRing><gml:posList>");
                }

                JSONArray arr2 = arr.getJSONArray(j);
                for (int k = 0; k < arr2.length(); k++) {
                    requestData
                            .append(arr2.getJSONObject(k).getString("x") + " " + arr2.getJSONObject(k).getString("y"));
                    if (k < (arr2.length() - 1)) {
                        requestData.append(" ");
                    }
                }

                if (j > 0) {
                    requestData.append("</gml:posList></gml:LinearRing></gml:interior>");
                } else {
                    requestData.append("</gml:posList></gml:LinearRing></gml:exterior>");
                }
            }
            requestData.append("</gml:Polygon></gml:polygonMember>");
        }
        requestData.append("</gml:MultiPolygon></" + geometryProperty + ">");
    }

}

