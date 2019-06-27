package fi.nls.oskari.control.feature;

import com.vividsolutions.jts.geom.*;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.Feature;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
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
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public abstract class AbstractFeatureHandler extends RestActionHandler {

    public final static String CACHKE_KEY_PREFIX = "WFSImage_";

    private OskariLayerService layerService;
    private PermissionsService permissionsService;
    private WFSLayerConfigurationService layerConfigurationService;
    private static final Set<String> ALLOWED_GEOM_TYPES = ConversionHelper.asSet("multipoint",
            "multilinestring", "multipolygon");
    private GeometryFactory gf = new GeometryFactory();

    @Override
    public void init() {
        super.init();
        layerService = new OskariLayerServiceMybatisImpl();
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

    protected Feature getFeature(JSONObject jsonObject) throws ActionParamsException, JSONException, FactoryException {
        Feature feature = new Feature();
        OskariLayer layer = getLayer(jsonObject.optString("layerId"));
        String srsName = JSONHelper.getStringFromJSON(jsonObject, "srsName", "EPSG:3067");
        CoordinateReferenceSystem crs = CRS.decode(srsName);
        WFSLayerConfiguration lc = getWFSConfiguration(layer.getId());

        feature.setLayerName(layer.getName());
        feature.setNamespace(lc.getFeatureNamespace());
        feature.setNamespaceURI(lc.getFeatureNamespaceURI());
        feature.setGMLGeometryProperty(lc.getGMLGeometryProperty());
        feature.setId(jsonObject.getString("featureId"));

        if(jsonObject.has("featureFields")) {
            JSONArray jsonArray = jsonObject.getJSONArray("featureFields");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject property = jsonArray.getJSONObject(i);
                feature.addProperty(property.getString("key"), property.getString("value"));
            }
        }

        if (jsonObject.has("geometries")) {
            JSONObject geometries = jsonObject.getJSONObject("geometries");
            String geometryType = geometries.getString("type");
            if(!isAllowedGeomType(geometryType)) {
                throw new ActionParamsException("Invalid geometry type: " + geometryType);
            }
            JSONArray data = geometries.getJSONArray("data");
            Geometry geometry = getGeometry(geometryType, data, getSrid(srsName, 3067));
            if(ProjectionHelper.isFirstAxisNorth(crs)) {
                // reverse xy
                ProjectionHelper.flipFeatureYX(geometry);
            }
            feature.setGeometry(geometry);
        }
        return feature;
    }

    protected int getSrid(String srsName, int defaultValue) {
        if (srsName != null) {
            int i = srsName.lastIndexOf(':');
            if (i > 0) {
                srsName = srsName.substring(i + 1);
            }
            try {
                return Integer.parseInt(srsName);
            } catch (NumberFormatException ignroe) {}
        }
        return defaultValue;
    }


    protected Geometry getGeometry(String geometryType, JSONArray data, int srid) throws ActionParamsException, JSONException {
        if ("multipoint".equals(geometryType)) {
            return getMultipoint(srid, data);
        } else if ("multilinestring".equals(geometryType)) {
            return getMultiLineStringGeometries(srid, data);
        } else if ("multipolygon".equals(geometryType)) {
            return getMultiPolygonGeometries(srid, data);
        }
        throw new ActionParamsException("Unknown type");
    }

    protected Geometry getMultipoint(int srid, JSONArray data) throws JSONException {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            Point point = gf.createPoint(new Coordinate(
                    Double.parseDouble(data.getJSONObject(i).getString("x")),
                    Double.parseDouble(data.getJSONObject(i).getString("y")))
            );
            points.add(point);
        }
        Geometry geometry = gf.createMultiPoint(points.toArray(new Point[points.size()]));
        geometry.setSRID(srid);

        return geometry;
    }

    protected Geometry getMultiLineStringGeometries(int srid, JSONArray data) throws JSONException {
        List<LineString> lines = new ArrayList<>();
        
        for (int lineIndex = 0; lineIndex < data.length(); lineIndex++) {
            List<Coordinate> coordinates = new ArrayList<>();
            JSONArray lineCoordinates = data.getJSONArray(lineIndex);
            for (int coordinateIndex = 0; coordinateIndex < lineCoordinates.length(); coordinateIndex++) {
                Coordinate coordinate = new Coordinate(
                        Double.parseDouble(lineCoordinates.getJSONObject(coordinateIndex).getString("x")),
                        Double.parseDouble(lineCoordinates.getJSONObject(coordinateIndex).getString("y"))
                );
                coordinates.add(coordinate);
            }
            LineString lineString = gf.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
            lines.add(lineString);
        }
        Geometry geometry  = gf.createMultiLineString(lines.toArray(new LineString[lines.size()]));
        geometry.setSRID(srid);

        return geometry;
    }

    protected Geometry getMultiPolygonGeometries(int srid, JSONArray data) throws JSONException {
        List<Polygon> polygons = new ArrayList<>();
        for (int polygonIdx = 0; polygonIdx < data.length(); polygonIdx++) {
            JSONArray linearRings = data.getJSONArray(polygonIdx);
            for (int ringIndex = 0; ringIndex < linearRings.length(); ringIndex++) {

                List<Coordinate> coordinates = new ArrayList<>();
                JSONArray currentRing = linearRings.getJSONArray(ringIndex);

                for (int coordinateIndex = 0; coordinateIndex < currentRing.length(); coordinateIndex++) {
                    Coordinate coordinate = new Coordinate(
                            Double.parseDouble(currentRing.getJSONObject(coordinateIndex).getString("x")),
                            Double.parseDouble(currentRing.getJSONObject(coordinateIndex).getString("y"))
                    );
                    coordinates.add(coordinate);
                }

                Polygon polygon = gf.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
                polygons.add(polygon);
            }
        }

        Geometry geometry = gf.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
        geometry.setSRID(srid);

        return geometry;

    }

    protected void flushLayerTilesCache(int layerId) {
        Set<String> keys = JedisManager.keys(CACHKE_KEY_PREFIX + Integer.toString(layerId));
        for (String key : keys) {
            JedisManager.delAll(key);
        }
    }
}
