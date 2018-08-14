package fi.nls.oskari.control.feature;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;


public abstract class AbstractFeatureHandler extends RestActionHandler {

    public final static String CACHKE_KEY_PREFIX = "WFSImage_";

    private OskariLayerService layerService;
    private PermissionsService permissionsService;
    private WFSLayerConfigurationService layerConfigurationService;
    private static final Set<String> ALLOWED_GEOM_TYPES = ConversionHelper.asSet("multipoint", "multilinestring", "multipolygon");

    @Override
    public void init() {
        super.init();
        layerService = new OskariLayerServiceIbatisImpl();
        permissionsService = new PermissionsServiceIbatisImpl();
        layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();
    }

    protected OskariLayer getLayer(String id) throws ActionParamsException {
        return layerService.find(getLayerId(id));
    }

    protected WFSLayerConfiguration getWFSConfiguration(int id) throws ActionParamsException {
        return layerConfigurationService.findConfiguration(id);
    }

    protected boolean canEdit(OskariLayer layer, User user) {
        final Resource resource = permissionsService.findResource(new OskariLayerResource(layer));
        return resource.hasPermission(user, Permissions.PERMISSION_TYPE_EDIT_LAYER_CONTENT);
    }

    protected int getLayerId(String layerId) throws ActionParamsException {
        int id = ConversionHelper.getInt(layerId, -1);
        if (id == -1) {
            throw new ActionParamsException("Missing layer id");
        }
        return id;
    }

    protected String postPayload(OskariLayer layer, String payload) throws ActionException {

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(layer.getUsername(), layer.getPassword());
        credsProvider.setCredentials(AuthScope.ANY, credentials);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setDefaultCredentialsProvider(credsProvider);

        HttpPost request = new HttpPost(layer.getUrl());
        request.addHeader(IOHelper.HEADER_CONTENTTYPE, IOHelper.CONTENT_TYPE_XML);
        request.setEntity(new StringEntity(payload, "UTF-8"));

        HttpClient httpClient = httpClientBuilder.build();
        try {
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            if (responseString == null) {
                throw new ActionParamsException("Didn't get any response from service");
            }
            return responseString;
        } catch (IOException ex) {
            throw new ActionException("Error posting the WFS-T message to service", ex);
        }
    }

    protected boolean isAllowedGeomType(String type) {
        if (type == null) {
            return false;
        }
        return ALLOWED_GEOM_TYPES.contains(type);
    }

    protected String getGeometry(String geometryType, JSONArray data, String srsName) throws ActionParamsException, JSONException {
        if ("multipoint".equals(geometryType)) {
            return getMultipoint(srsName, data);
        } else if ("multilinestring".equals(geometryType)) {
            return getMultiLineStringGeometries(srsName, data);
        } else if ("multipolygon".equals(geometryType)) {
            return getMultiPolygonGeometries(srsName, data);
        }
        throw new ActionParamsException("Unknown type");
    }

    protected String getMultipoint(String srsName, JSONArray data) throws JSONException {
        StringWriter writer = new StringWriter();
        writer.append("<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/gml\" srsName=\"");
        writer.append(srsName);
        writer.append("\">");
        for (int i = 0; i < data.length(); i++) {
            writer.append("<gml:pointMember><gml:Point><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">"
                    + data.getJSONObject(i).getString("x") + "," + data.getJSONObject(i).getString("y")
                    + "</gml:coordinates></gml:Point></gml:pointMember>");
        }
        writer.append("</gml:MultiPoint>");
        return writer.toString();
    }

    protected String getMultiLineStringGeometries(String srsName, JSONArray data) throws JSONException {
        StringWriter writer = new StringWriter();
        writer.append("<gml:MultiLineString xmlns:gml=\"http://www.opengis.net/gml\" srsName=\"");
        writer.append(srsName);
        writer.append("\">");
        for (int lineIndex = 0; lineIndex < data.length(); lineIndex++) {
            writer.append("<gml:lineStringMember><gml:LineString><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">");
            JSONArray lineCoordinates = data.getJSONArray(lineIndex);
            for (int coordinateIndex = 0; coordinateIndex < lineCoordinates.length(); coordinateIndex++) {
                JSONObject coord = lineCoordinates.getJSONObject(coordinateIndex);
                writer.append(coord.getString("x") + "," + coord.getString("y"));
                if (coordinateIndex < (lineCoordinates.length() - 1)) {
                    writer.append(" ");
                }
            }
            writer.append("</gml:coordinates></gml:LineString></gml:lineStringMember>");
        }
        writer.append("</gml:MultiLineString>");
        return writer.toString();
    }

    protected String getMultiPolygonGeometries(String srsName, JSONArray data) throws JSONException {
        StringWriter writer = new StringWriter();
        writer.append("<gml:MultiPolygon xmlns:gml=\"http://www.opengis.net/gml\" srsName=\"");
        writer.append(srsName);
        writer.append("\">");

        for (int polygonIdx = 0; polygonIdx < data.length(); polygonIdx++) {
            writer.append("<gml:polygonMember><gml:Polygon>");
            JSONArray linearRings = data.getJSONArray(polygonIdx);
            for (int ringIndex = 0; ringIndex < linearRings.length(); ringIndex++) {
                boolean isExteriorRing = ringIndex == 0;
                if (isExteriorRing) {
                    writer.append("<gml:exterior>");
                } else {
                    writer.append("<gml:interior>");
                }
                writer.append("<gml:LinearRing><gml:posList>");

                JSONArray currentRing = linearRings.getJSONArray(ringIndex);
                for (int coordinateIndex = 0; coordinateIndex < currentRing.length(); coordinateIndex++) {
                    JSONObject coord = currentRing.getJSONObject(coordinateIndex);
                    writer.append(coord.getString("x") + " " + coord.getString("y"));
                    if (coordinateIndex < (currentRing.length() - 1)) {
                        writer.append(" ");
                    }
                }

                writer.append("</gml:posList></gml:LinearRing>");
                if (isExteriorRing) {
                    writer.append("</gml:exterior>");
                } else {
                    writer.append("</gml:interior>");
                }
            }
            writer.append("</gml:Polygon></gml:polygonMember>");
        }

        writer.append("</gml:MultiPolygon>");
        return writer.toString();
    }

    protected void flushLayerTilesCache(int layerId) {
        Set<String> keys = JedisManager.keys(CACHKE_KEY_PREFIX + Integer.toString(layerId));
        for (String key : keys) {
            JedisManager.delAll(key);
        }
    }
}
