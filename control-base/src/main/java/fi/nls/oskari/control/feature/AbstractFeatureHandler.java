package fi.nls.oskari.control.feature;


import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.locationtech.jts.geom.*;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.*;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.Feature;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.domain.map.wfs.WFSLayerCapabilities;

import fi.nls.oskari.map.geometry.ProjectionHelper;

import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.ResourceType;
import org.oskari.service.util.ServiceFactory;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;


public abstract class AbstractFeatureHandler extends RestActionHandler {

    public final static String CACHE_KEY_PREFIX = "WFSImage_";
    private static Logger LOG = LogFactory.getLogger(AbstractFeatureHandler.class);

    private OskariLayerService layerService;
    private PermissionService permissionsService;

    private static final Set<String> ALLOWED_GEOM_TYPES = ConversionHelper.asSet("multipoint",
            "multilinestring", "multipolygon");
    private static final GeometryFactory gf = new GeometryFactory();
    private static final GeoJsonReader GEOJSON_READER = new GeoJsonReader(gf);

    @Override
    public void init() {
        super.init();
        layerService = ServiceFactory.getMapLayerService();
        permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
    }

    protected OskariLayer getLayer(String id) throws ActionParamsException {
        return layerService.find(getLayerId(id));
    }

    protected boolean canEdit(OskariLayer layer, User user) {
        return permissionsService.findResource(ResourceType.maplayer, Integer.toString(layer.getId()))
                .filter(r -> r.hasPermission(user, PermissionType.EDIT_LAYER_CONTENT)).isPresent();
    }

    protected int getLayerId(String layerId) throws ActionParamsException {
        int id = ConversionHelper.getInt(layerId, -1);
        if (id == -1) {
            throw new ActionParamsException("Missing layer id");
        }
        return id;
    }

    protected String postPayload(String username, String password, String payload, String url) throws ActionException {
        try {
            HttpURLConnection conn = IOHelper.getConnection(url, username, password);
            IOHelper.writeHeader(conn, IOHelper.HEADER_CONTENTTYPE, IOHelper.CONTENT_TYPE_XML);
            IOHelper.writeToConnection(conn, payload);
            String responseString = IOHelper.readString(conn);
            if (responseString == null) {
                throw new ActionParamsException("Didn't get any response from service");
            }
            return responseString;
        } catch (IOException e) {
            throw new ActionException("Error posting the WFS-T message to service", e);
        }
    }

    protected boolean isAllowedGeomType(String type) {
        if (type == null) {
            return false;
        }
        return ALLOWED_GEOM_TYPES.contains(type);
    }
    protected Feature getFeature(JSONObject jsonObject) throws ActionParamsException, JSONException, FactoryException {
        String srsName = JSONHelper.getStringFromJSON(jsonObject, "srsName", "EPSG:3067");
        return getFeature(jsonObject, jsonObject.optString("layerId"), srsName, jsonObject.getString("featureId"));
    }

    // protected Feature getFeature(JSONObject jsonObject) throws ActionParamsException, JSONException, FactoryException {
    protected Feature getFeature(JSONObject jsonObject, String layerId, String srsName, String featureId) throws ActionParamsException, JSONException, FactoryException {
        boolean flipFeature = PropertyUtil.getOptional("actionhandler.AbstractFeatureHandler.forceXY", false);
        OskariLayer layer = getLayer(layerId);
        Feature feature = new Feature();
        CoordinateReferenceSystem crs = CRS.decode(srsName);
        WFSLayerAttributes attrs = new WFSLayerAttributes(layer.getAttributes());
        WFSLayerCapabilities caps = new WFSLayerCapabilities(layer.getCapabilities());
        String layerName = layer.getName();
        // remove prefix from layername
        if (layerName.indexOf(":") != -1){
            layerName = (layerName.substring(layerName.indexOf(":")+1)).trim();
        }

        feature.setLayerName(layerName);
        feature.setNamespaceURI(attrs.getNamespaceURL());
        feature.setGMLGeometryProperty(caps.getGeometryAttribute());
        feature.setId(featureId);

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

            if(ProjectionHelper.isFirstAxisNorth(crs) || flipFeature) {
                // reverse xy
                ProjectionHelper.flipFeatureYX(geometry);
            }
            feature.setGeometry(geometry);
        }

        // support GeoJSON feature
        if (jsonObject.has("properties")) {
            JSONObject props = jsonObject.getJSONObject("properties");
            Iterator keys = props.keys();
            while (keys.hasNext()) {
                String name = (String) keys.next();
                feature.addProperty(name, props.getString(name));
            }
        }
        if (jsonObject.has("geometry")) {
            String geojson = jsonObject.toString();
            try {
                feature.setGeometry(GEOJSON_READER.read(geojson));
            } catch (ParseException e) {
                LOG.debug(e, "Error parsing feature:\n", geojson);
                throw new ActionParamsException("Couldn't parse feature: " + geojson);
            }
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
        Set<String> keys = JedisManager.keys(CACHE_KEY_PREFIX + Integer.toString(layerId));
        for (String key : keys) {
            JedisManager.delAll(key);
        }
    }

    protected void flushLayerTilesCache(Map<Integer, OskariLayer> layers) {
        for (Integer layerId : layers.keySet()) {
            Set<String> keys = JedisManager.keys(CACHE_KEY_PREFIX + Integer.toString(layerId));
            for (String key : keys) {
                JedisManager.delAll(key);
            }
        }
    }

    protected Map<Integer, OskariLayer> getLayers(JSONArray paramFeatures) throws JSONException, ActionParamsException {
        Map<Integer, OskariLayer> layers = new HashMap<>();
        for (int i = 0; i < paramFeatures.length(); i++) {
            JSONObject featureJSON = paramFeatures.getJSONObject(i);
            OskariLayer layer = getLayer(featureJSON.optString("layerId"));
            layers.put(layer.getId(), layer);
        }
        return layers;
    }

    protected void hasUserPermissionEditLayers(Map<Integer, OskariLayer> layers, User user) throws ActionDeniedException {
        for (Integer layerId : layers.keySet()) {
            if (!canEdit(layers.get(layerId), user)) {
                throw new ActionDeniedException("User doesn't have edit permission for layer: " + layerId);
            }
        }
    }

    /**
     * Takes workspace prefix from layer name (before ':') and puts it into the layer url before '/wfs' or '/ows'
     * NOTE! May not work with other than geoserver
     * @param  layerName  String layer technical name
     */
    protected String getURLForNamespace(String layerName, String url){
        
        if(layerName.indexOf(":") != -1){
            String prefix = layerName.split(":")[0];
            if(!(url.contains(prefix))){
                if(url.contains("/ows")){
                    url = url.replace("/ows","/"+prefix+"/ows");
                }
                else if (url.contains("/wfs")){
                    url = url.replace("/wfs","/"+prefix+"/wfs");
                }
            }
        }
        return url;
    }
}
