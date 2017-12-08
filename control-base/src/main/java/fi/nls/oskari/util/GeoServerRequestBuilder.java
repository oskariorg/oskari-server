package fi.nls.oskari.util;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.MyPlacesServiceMybatisImpl;
import fi.nls.oskari.service.OskariComponentManager;

import org.apache.axiom.om.*;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.filter.v1_0.OGCConfiguration;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.IOP.TAG_JAVA_CODEBASE;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import java.util.Arrays;
import java.util.List;

public class GeoServerRequestBuilder {

    private static final Logger log = LogFactory.getLogger(GeoServerRequestBuilder.class);

    private static final String TAG_LAYER= "categories";
    private static final String TAG_FEATURES = "my_places";
    private static final String TAG_UUID = "uuid";
    private static final String TYPE_LAYERS = "feature:categories";
    private static final String TYPE_FEATURES = "feature:my_places";
    private static final String ATTR_UUID = "uuid";
    private static final String ATTR_LAYERID = "category_id";
    private static final String ATTR_FID = "fid";
    private static final String TUPLE_SEPARATOR = " ";
    private static final String DECIMAL_SEPARATOR = ".";
    private static final String COORDINATE_SEPARATOR = ",";

    //private static final String GML_VERSION = "3.1.1";
    private static final String GET_FEATURE_OUTPUT_FORMAT = "application/json";
    //default: "text/xml"
    private static final String SRS_NAME = "EPSG:3067"; //TODO db/props
    // urn:x-ogc:def:crs: + EPSG:3067
    private static final String FID_PREFIX_LAYERS = "categories.";
    private static final String FID_PREFIX_FEATURES = "my_places.";

    private static final String FEATURE_NS_URI = "http://www.oskari.org"; //TODO db/props
    //private static final String FEATURE_NS = "oskari";
    private static final String GEOMETRY_NAME = "geometry"; //TODO db/props
    //private static final String JSON_LAYERS = "layers";
    //private static final String JSON_FEATURES = "features";
    private static final String JSON_ID = "id"; // fid = prefix + id

    private static final String WFS_VERSION_FEATURES = "1.0.0";
    private static final String WFS_VERSION_LAYERS = "1.1.0";
    private static final String WFST_VERSION_FEATURES = "1.1.0";
    private static final String WFST_VERSION_LAYERS = "1.1.0";
    private static final String WFS_SCHEMA_BASE = "http://schemas.opengis.net/wfs/";
    private static final String WFS_SCHEMA = "/wfs.xsd";
    private static final String WFST_SCHEMA = "/WFS-transaction.xsd";
    //http://schemas.opengis.net/wfs/1.1.0/WFS-transaction.xsd

    private OMNamespace xmlSchemaInstance = null;
    private OMNamespace wfsNS = null;
    private OMNamespace featureNS = null;
    private OMNamespace ogcNS = null;
    private OMNamespace gmlNS = null;
    private OMAttribute csAttribute;
    private OMAttribute tsAttribute;
    private OMAttribute decimalAttribute;

    private OMFactory factory = null;
    private MyPlacesService service;

    private static final List<String> LAYERS_LIST = Arrays.asList("category_name", "default", "stroke_width",
            "stroke_color", "fill_color", "dot_color", "dot_size", "border_width", "border_color",
            "dot_shape", "stroke_linejoin", "fill_pattern", "stroke_linecap", "stroke_dasharray", "border_linejoin",
            "border_dasharray");

    private static final List<String> FEATURES_LIST = Arrays.asList("name", "place_desc", "attention_text", "link",
            "image_url");

    public GeoServerRequestBuilder() {
        factory = OMAbstractFactory.getOMFactory();
        service = OskariComponentManager.getComponentOfType(MyPlacesService.class);

        xmlSchemaInstance = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        wfsNS = factory.createOMNamespace("http://www.opengis.net/wfs", "wfs");
        featureNS = factory.createOMNamespace(FEATURE_NS_URI, "feature");
        ogcNS = factory.createOMNamespace("http://www.opengis.net/ogc", "ogc");
        gmlNS = factory.createOMNamespace("http://www.opengis.net/gml", "gml");

        csAttribute = factory.createOMAttribute("cs", null, COORDINATE_SEPARATOR);
        tsAttribute = factory.createOMAttribute("ts", null, TUPLE_SEPARATOR);
        decimalAttribute = factory.createOMAttribute("decimal", null, DECIMAL_SEPARATOR);
    }

    public OMElement buildLayersGet(User user) throws Exception {
        return buildGetFeature(ATTR_UUID, user.getUuid(), TYPE_LAYERS, WFS_VERSION_LAYERS);
    }

    public OMElement buildLayersInsert(User user, String payload) throws Exception {

        OMElement transaction = buildWFSTRootNode(WFST_VERSION_LAYERS);
        JSONArray jsonArray = new JSONArray(payload);

        for (int i = 0; i < jsonArray.length(); ++i) {
            OMElement insert = factory.createOMElement("Insert", wfsNS);
            OMElement layer = factory.createOMElement(TAG_LAYER, featureNS);
            OMElement uuidElem = factory.createOMElement("uuid", featureNS);
            uuidElem.setText(user.getUuid());
            layer.addChild(uuidElem);

            for (String property : LAYERS_LIST) {
                layer.addChild(getElement(jsonArray.getJSONObject(i), property, featureNS));
            }
            insert.addChild(layer);
            transaction.addChild(insert);
        }
        return transaction;
    }

    public OMElement buildLayersUpdate(User user, String payload) throws Exception {
        OMElement transaction  = buildWFSTRootNode(WFST_VERSION_LAYERS);
        JSONArray jsonArray = new JSONArray(payload);

        for (int i = 0; i < jsonArray.length(); ++i) {
            long id = jsonArray.getJSONObject(i).getLong(JSON_ID);
            if (!service.canModifyCategory(user, id)){
                throw new ActionDeniedException("Tried to modify category: " + id);
            }

            OMElement update = factory.createOMElement("Update", wfsNS);
            update.addAttribute("typeName",TYPE_LAYERS, null);
            update.declareNamespace(featureNS);
            update.addChild(buildPropertyElement("uuid", user.getUuid()));

            for (String property : LAYERS_LIST) {
                String propertyValue = jsonArray.getJSONObject(i).get(property).toString();
                update.addChild(buildPropertyElement(property, propertyValue));
            }
            update.addChild(buildFidFilter(FID_PREFIX_LAYERS + id));
            transaction.addChild(update);
        }
        return transaction;
    }

    public OMElement buildLayersDelete(User user, String ids) throws Exception {
        OMElement transaction = buildWFSTRootNode(WFS_VERSION_LAYERS);
        String [] idList = ids.split(",");
        for (String id: idList){
            if (!service.canModifyCategory(user, Long.parseLong(id))){
                throw new ActionDeniedException("Tried to delete category: " + id);
            }
            OMElement delete = factory.createOMElement("Delete", wfsNS);
            delete.addAttribute("typeName", TYPE_LAYERS, null);
            delete.declareNamespace(featureNS);
            String fid = FID_PREFIX_LAYERS + id;
            delete.addChild(buildFidFilter(fid));
            transaction.addChild(delete);
        }
        return transaction;
    }

    public OMElement buildFeaturesGet(User user) throws Exception {
        return buildGetFeature(ATTR_UUID, user.getUuid(), TYPE_FEATURES, WFS_VERSION_FEATURES);
    }

    public OMElement buildFeaturesGetByLayer(User user, String layerId) throws Exception {
        if (!service.canModifyCategory(user, Long.parseLong(layerId))){
            throw new ActionDeniedException("Tried to get features from my places layer: " + layerId);
        }
        return buildGetFeature(ATTR_LAYERID, layerId, TYPE_FEATURES, WFS_VERSION_FEATURES);
    }
    //TODO: ids -> String/String[]/JSONArray,.. ??
    public OMElement buildFeaturesGetByIds(User user, String ids) throws Exception {
        OMElement root = buildWFSRootNode("GetFeature", WFS_VERSION_FEATURES);
        root.addAttribute("outputFormat", GET_FEATURE_OUTPUT_FORMAT, null);

        OMElement query = factory.createOMElement("Query", wfsNS);
        query.declareNamespace(featureNS);
        query.addAttribute("typeName", TYPE_FEATURES, null);
        query.addAttribute("srsName", SRS_NAME, null);

        OMElement filter = factory.createOMElement("Filter", ogcNS);
        String [] idList = ids.split(",");
        String fid;
        for (String id : idList){
            if (!service.canModifyPlace(user, Long.parseLong(id))){
                throw new ActionDeniedException("Tried to get feature: " + id);
            }
            fid = FID_PREFIX_FEATURES + id;
            filter.addChild(buildFid(fid));
        }
        query.addChild(filter);
        root.addChild(query);

        return root;
    }

    public OMElement buildFeaturesInsert(User user, String payload) throws Exception {
        OMElement transaction = buildWFSTRootNode(WFST_VERSION_FEATURES);
        JSONArray jsonArray = new JSONArray(payload);

        for (int i = 0; i < jsonArray.length(); ++i) {
            long categoryId = jsonArray.getJSONObject(i).getLong(ATTR_LAYERID);

            if (!service.canInsert(user, categoryId)){
                throw new ActionDeniedException("Tried to insert feature into category: " + categoryId);
            }
            OMElement insert = factory.createOMElement("Insert", wfsNS);

            OMElement myPlaces = factory.createOMElement(TAG_FEATURES, featureNS);

            OMElement geometry = factory.createOMElement(GEOMETRY_NAME, featureNS);
            geometry.addChild(getGeometry(jsonArray.getJSONObject(i).getJSONObject("geometry")));
            myPlaces.addChild(geometry);

            OMElement uuidElem = factory.createOMElement(TAG_UUID, featureNS);
            uuidElem.setText(user.getUuid());
            myPlaces.addChild(uuidElem);

            OMElement categoryElem = factory.createOMElement(ATTR_LAYERID, featureNS);
            categoryElem.setText(Long.toString(categoryId));
            myPlaces.addChild(categoryElem);

            for (String property : FEATURES_LIST) {
                myPlaces.addChild(getElement(jsonArray.getJSONObject(i).getJSONObject("properties"), property, featureNS));
            }

            insert.addChild(myPlaces);
            transaction.addChild(insert);
        }
        return transaction;
    }

    public OMElement buildFeaturesUpdate(User user, String payload) throws Exception {

        OMElement transaction = buildWFSTRootNode(WFST_VERSION_FEATURES);
        JSONArray jsonArray = new JSONArray(payload);
        for (int i = 0; i < jsonArray.length(); ++i) {
            long id = jsonArray.getJSONObject(i).getLong(JSON_ID);
            if (!service.canModifyPlace(user, id)){
                throw new ActionDeniedException("Tried to modify place: " + id);
            }

            OMElement update = factory.createOMElement("Update", wfsNS);
            update.addAttribute("typeName", TYPE_FEATURES, null);
            update.declareNamespace(featureNS);

            OMElement gml = getGeometry(jsonArray.getJSONObject(i).getJSONObject("geometry"));
            update.addChild(buildPropertyElement(GEOMETRY_NAME, gml));
            JSONObject properties = jsonArray.getJSONObject(i).getJSONObject("properties");

            String propertyValue;
            for (String property : FEATURES_LIST) {
                propertyValue = properties.get(property).toString();
                update.addChild(buildPropertyElement(property, propertyValue));
            }
            update.addChild(buildPropertyElement("uuid", user.getUuid()));
            String categoryId = jsonArray.getJSONObject(i).get("category_id").toString();
            update.addChild(buildPropertyElement("category_id", categoryId));

            update.addChild(buildFidFilter(FID_PREFIX_FEATURES + id));

            transaction.addChild(update);
        }
        return transaction;
    }

    public OMElement buildFeaturesDelete(User user, String ids) throws Exception {
        OMElement transaction = buildWFSTRootNode(WFST_VERSION_FEATURES);
        String [] idList = ids.split(",");
        for (String id: idList){
            if (!service.canModifyPlace(user, Long.parseLong(id))){
                throw new ActionDeniedException("Tried to delete place: " + id);
            }
            OMElement delete = factory.createOMElement("Delete", wfsNS);
            delete.addAttribute("typeName", TYPE_FEATURES, null);
            delete.declareNamespace(featureNS);

            String fid = FID_PREFIX_FEATURES + id;
            delete.addChild(buildFidFilter(fid));

            transaction.addChild(delete);
        }
        return transaction;
    }

    private OMElement buildGetFeature(String filterProperty, String filterValue, String type, String version) throws Exception {
        OMElement root = buildWFSRootNode("GetFeature", version);
        root.addAttribute("outputFormat", GET_FEATURE_OUTPUT_FORMAT, null);

        OMElement query = factory.createOMElement("Query", wfsNS);
        query.declareNamespace(featureNS);
        query.addAttribute("typeName", type, null);
        query.addAttribute("srsName", SRS_NAME, null);

        query.addChild(buildFilter("PropertyIsEqualTo", filterProperty, filterValue, false));
        root.addChild(query);

        return root;
    }
    //WFS
    private OMElement buildWFSRootNode (String wfsType, String version) throws Exception {
        OMElement root = factory.createOMElement(wfsType, wfsNS);
        OMAttribute schemaLocation = factory.createOMAttribute("schemaLocation",
                xmlSchemaInstance,
                "http://www.opengis.net/wfs" + " " + WFS_SCHEMA_BASE + version + WFS_SCHEMA);

        OMAttribute versionElement = factory.createOMAttribute("version", null, version);
        OMAttribute serviceElement = factory.createOMAttribute("service", null, "WFS");

        root.addAttribute(schemaLocation);
        root.addAttribute(versionElement);
        root.addAttribute(serviceElement);

        return root;
    }
    //WFS-T
    private OMElement buildWFSTRootNode (String version) throws Exception {
        OMElement root = factory.createOMElement("Transaction", wfsNS);
        OMAttribute schemaLocation = factory.createOMAttribute("schemaLocation",
                xmlSchemaInstance,
                "http://www.opengis.net/wfs" + " " + WFS_SCHEMA_BASE + version + WFS_SCHEMA);

        OMAttribute versionElement = factory.createOMAttribute("version", null, version);
        OMAttribute serviceElement = factory.createOMAttribute("service", null, "WFS");

        root.addAttribute(schemaLocation);
        root.addAttribute(versionElement);
        root.addAttribute(serviceElement);

        return root;
    }

    private OMElement getElement(JSONObject jsonObject, String fieldName, OMNamespace ns) throws Exception {
        String value = jsonObject.getString(fieldName);
        OMElement var = factory.createOMElement(fieldName, ns);
        var.setText(value);

        return var;
    }

    private OMElement buildPropertyElement(String propertyName, String propertyValue) throws Exception {
        OMElement property = factory.createOMElement("Property", wfsNS);

        OMElement name = factory.createOMElement("Name", wfsNS);
        name.setText(propertyName);
        property.addChild(name);

        OMElement value = factory.createOMElement("Value", wfsNS);
        value.setText(propertyValue);
        property.addChild(value);

        return property;
    }

    private OMElement buildPropertyElement(String propertyName, OMElement propertyElem) throws Exception {
        OMElement property = factory.createOMElement("Property", wfsNS);

        OMElement name = factory.createOMElement("Name", wfsNS);
        name.setText(propertyName);
        property.addChild(name);

        OMElement value = factory.createOMElement("Value", wfsNS);
        value.addChild(propertyElem);
        property.addChild(value);

        return property;
    }

    private OMElement buildFilter (String operator, String name, String value, boolean matchCase){
        OMElement filterElem = factory.createOMElement("Filter", ogcNS);
        OMElement typeElem = factory.createOMElement(operator, ogcNS);
        if (matchCase){
            typeElem.addAttribute("matchCase", "true", null);
        }
        OMElement property = factory.createOMElement("PropertyName", ogcNS);
        property.setText(name);
        typeElem.addChild(property);

        OMElement literal = factory.createOMElement("Literal", ogcNS);
        literal.setText(value);
        typeElem.addChild(literal);
        filterElem.addChild(typeElem);
        return filterElem;
    }

    private OMElement buildFidFilter(String fid) {
        OMElement filter = factory.createOMElement("Filter", ogcNS);
        OMElement property = factory.createOMElement("FeatureId", ogcNS);
        property.addAttribute(ATTR_FID, fid, null);
        filter.addChild(property);
        return filter;
    }

    private OMElement buildFid(String fid) {
        OMElement fidElem = factory.createOMElement("FeatureId", ogcNS);
        fidElem.addAttribute(ATTR_FID, fid, null);
        return fidElem;
    }

    private OMElement getGeometry(JSONObject geometryJson) throws Exception {
        String geometryType = geometryJson.getString("type");
        JSONArray coordsJson;
        //GeometryCollection's geometry objects must be same type (Point, LineString, Polygon) not Multi geometry
        //TODO: add Multi geometry handling for GeometryCollection
        if (geometryType.equals("GeometryCollection")){
            JSONArray geometries = geometryJson.getJSONArray("geometries");
            coordsJson= new JSONArray();
            for (int i = 0; i < geometries.length(); i++){
                coordsJson.put(geometries.getJSONObject(i).getJSONArray("coordinates"));
            }
            //TODO:
            return getGeometry("Multi"+ geometries.getJSONObject(0).getString("type"), coordsJson);
        }else {
            //Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon
            coordsJson = geometryJson.getJSONArray("coordinates");
            return getGeometry(geometryType, coordsJson);
        }
    }

    private OMElement getGeometry(String geometryType, JSONArray coords) throws Exception {
        OMElement geometry = factory.createOMElement(geometryType, gmlNS);

        OMElement memberElem;

        switch (geometryType){
            case "Point":
                //single position [x, y]
                geometry = createPoint(coords);
                break;
            case "MultiPoint":
                //an array of positions
                for (int i = 0; i < coords.length();i++){
                    memberElem = createMember("pointMember");
                    memberElem.addChild(createPoint(coords.getJSONArray(i)));
                    geometry.addChild(memberElem);
                }
                break;
            case "LineString":
                //array of two or more positions
                geometry = createLineString(coords);
                break;
            case "MultiLineString":
                //an array of LineString coordinate arrays
                for (int i = 0; i < coords.length();i++){
                    memberElem = createMember("lineStringMember");
                    memberElem.addChild(createLineString(coords.getJSONArray(i)));
                    geometry.addChild(memberElem);
                }
                break;
            case "Polygon":
                //array of linear ring (closed LineString) coordinate arrays, first linear ring is exterior ring
                geometry = createPolygon(coords);
                break;
            case "MultiPolygon":
                //an array of Polygon coordinate arrays
                for (int i = 0; i < coords.length();i++){
                    memberElem = createMember("polygonMember");
                    memberElem.addChild(createPolygon(coords.getJSONArray(i)));
                    geometry.addChild(memberElem);
                }
                break;
            default:
                throw new Exception("Illegal geometry type: " + geometryType);
        }
        geometry.addAttribute("srsName", SRS_NAME, null);
        return geometry;
    }
    private OMElement createPoint (JSONArray pointCoords) throws JSONException {
        OMElement geomElem =  factory.createOMElement("Point", gmlNS);
        String gmlCoords = pointCoords.join(COORDINATE_SEPARATOR);
        geomElem.addChild(createCoordinateElement(gmlCoords));
        return geomElem;
    }
    private OMElement createLineString (JSONArray lineCoords) throws JSONException {
        OMElement geomElem =  factory.createOMElement("LineString", gmlNS);
        String gmlCoords = parseGMLCoordinateFromPositionsArray(lineCoords);
        geomElem.addChild(createCoordinateElement(gmlCoords));
        return geomElem;
    }
    private OMElement createPolygon (JSONArray polygonCoords) throws JSONException {
        OMElement geomElem =  factory.createOMElement("Polygon", gmlNS);
        OMElement boundary;
        OMElement linearRing;
        String coord;
        //outerBoundary coords[0]
        boundary = factory.createOMElement("outerBoundaryIs", gmlNS);
        linearRing = factory.createOMElement("LinearRing", gmlNS);
        coord = parseGMLCoordinateFromPositionsArray(polygonCoords.getJSONArray(0));
        linearRing.addChild(createCoordinateElement(coord));
        boundary.addChild (linearRing);
        geomElem.addChild(boundary);
        //innerBoundaries (holes) 0-n, array [1-n+1]
        for (int i = 1; i < polygonCoords.length(); i++ ){
            boundary = factory.createOMElement("innerBoundaryIs", gmlNS);
            linearRing = factory.createOMElement("LinearRing", gmlNS);
            coord = parseGMLCoordinateFromPositionsArray(polygonCoords.getJSONArray(i));
            linearRing.addChild(createCoordinateElement(coord));
            boundary.addChild (linearRing);
            geomElem.addChild(boundary);
        }
        return geomElem;
    }

    private OMElement createMember (String type){
        OMElement memberElem = factory.createOMElement(type, gmlNS);
        return memberElem;
    }

    private String parseGMLCoordinateFromPositionsArray (JSONArray array) throws JSONException {
        // GeoJSON coordinates to GML coordinates
        //[[293738.4555,6827928.7191],[300970.4555,6827800.7191]]
        //-->
        //293738.4555,6827928.7191 300970.4555,6827800.7191
        String gml = "";
        for (int i = 0; i < array.length(); i++){
            gml += array.getJSONArray(i).join(COORDINATE_SEPARATOR);
            if (i < (array.length()-1)) {
                gml += TUPLE_SEPARATOR;
            }
        }
        return gml;
    }
    
    private OMElement createCoordinateElement (String gmlCoords){
        OMElement elem = factory.createOMElement("coordinates", gmlNS);
        elem.addAttribute(decimalAttribute);
        elem.addAttribute(csAttribute);
        elem.addAttribute(tsAttribute);
        elem.setText(gmlCoords);
        return elem;
    }

}
