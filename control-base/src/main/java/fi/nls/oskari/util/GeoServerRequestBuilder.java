package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import org.apache.axiom.om.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class GeoServerRequestBuilder {

    private static final Logger log = LogFactory.getLogger(GeoServerRequestBuilder.class);

    private static final String VERSION_1_1_0 = "1.1.0";
    private static final String VERSION_1_0_0 = "1.0.0";

    private static OMFactory factory = null;
    private static OMNamespace xmlSchemaInstance = null;
    private static OMNamespace wfsNameSpace = null;

    public GeoServerRequestBuilder() {

        factory = OMAbstractFactory.getOMFactory();

        xmlSchemaInstance = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        wfsNameSpace = factory.createOMNamespace("http://www.opengis.net/wfs", "wfs");
    }

    private static final List<String> LAYERS_LIST = Arrays.asList("category_name", "default", "stroke_width",
            "stroke_color", "fill_color", "uuid", "dot_color", "dot_size", "border_width", "border_color",
            "dot_shape", "stroke_linejoin", "fill_pattern", "stroke_linecap", "stroke_dasharray", "border_linejoin",
            "border_dasharray");


    private static final List<String> FEATURES_LIST = Arrays.asList("name", "place_desc", "attention_text", "link",
            "image_url", "category_id", "feature", "uuid");

    public OMElement buildLayersGet(String uuid) throws Exception {
        return buildGet(uuid, "feature:categories", VERSION_1_1_0);
    }

    public OMElement buildLayersInsert(String payload) throws Exception {

        OMElement root = buildWFSRootNode("Transaction", VERSION_1_1_0);

        OMElement transaction = factory.createOMElement("Insert", wfsNameSpace);
        OMNamespace feature = factory.createOMNamespace("http://www.oskari.org", "feature");

        OMElement categories = factory.createOMElement("categories", feature);

        JSONArray jsonArray = new JSONObject(payload).getJSONArray("categories");
        for (int i = 0; i < jsonArray.length(); ++i) {
            for (String property : LAYERS_LIST) {
                categories.addChild(getElement(jsonArray.getJSONObject(i), property, feature));
            }
        }

        transaction.addChild(categories);
        root.addChild(transaction);

        return root;
    }

    public OMElement buildLayersUpdate(String payload) throws Exception {

        OMElement root = buildWFSRootNode("Transaction", VERSION_1_1_0);

        OMNamespace feature = factory.createOMNamespace("http://www.oskari.org", "feature");
        OMElement transaction = factory.createOMElement("Update", feature);
        OMAttribute typeName = factory.createOMAttribute("typeName", null, "feature:categories");
        transaction.addAttribute(typeName);

        JSONArray jsonArray = new JSONObject(payload).getJSONArray("categories");
        for (int i = 0; i < jsonArray.length(); ++i) {
            for (String property : LAYERS_LIST) {
                transaction.addChild(buildPropertyElement(jsonArray.getJSONObject(i), property, feature));
            }
            transaction.addChild(buildIdFilter(jsonArray.getJSONObject(i).getString("category_id")));
        }
        root.addChild(transaction);

        return root;
    }


    public OMElement buildLayersDelete(String categoryId) throws Exception {

        OMElement root = buildWFSRootNode("Transaction", VERSION_1_1_0);

        OMNamespace feature = factory.createOMNamespace("http://www.oskari.org", "feature");
        OMElement transaction = factory.createOMElement("Delete", feature);
        OMAttribute typeName = factory.createOMAttribute("typeName", null, "feature:categories");
        transaction.addAttribute(typeName);

        transaction.addChild(buildIdFilter(categoryId));

        root.addChild(transaction);

        return root;
    }

    public OMElement buildFeaturesGet(String uuid) throws Exception {

        OMElement root = buildGet(uuid, "feature:my_places", VERSION_1_0_0);
        OMAttribute outputElement = factory.createOMAttribute("outputFormat", null, "application/json");
        root.addAttribute(outputElement);

        return root;
    }

    public OMElement buildFeaturesInsert(String payload) throws Exception {

        OMElement root = buildWFSRootNode("Transaction", VERSION_1_0_0);

        OMElement transaction = factory.createOMElement("Insert", wfsNameSpace);
        OMNamespace feature = factory.createOMNamespace("http://www.oskari.org", "feature");

        OMElement myPlaces = factory.createOMElement("my_places", feature);

        JSONArray jsonArray = new JSONObject(payload).getJSONArray("features");
        for (int i = 0; i < jsonArray.length(); ++i) {

            OMElement geometry = factory.createOMElement("geometry", feature);
            geometry.addChild(getGeometry(jsonArray.getJSONObject(i).getJSONObject("geometry")));
            myPlaces.addChild(geometry);

            for (String property : FEATURES_LIST) {
                myPlaces.addChild(getElement(jsonArray.getJSONObject(i).getJSONObject("properties"), property, feature));
            }
        }
        transaction.addChild(myPlaces);
        root.addChild(transaction);

        return root;
    }

    public OMElement buildFeaturesUpdate(String payload) throws Exception {

        OMElement root = buildWFSRootNode("Transaction", VERSION_1_0_0);

        OMElement transaction = factory.createOMElement("Update", wfsNameSpace);
        OMNamespace feature = factory.createOMNamespace("http://www.oskari.org", "feature");

        OMElement myPlaces = factory.createOMElement("my_places", feature);

        JSONArray jsonArray = new JSONObject(payload).getJSONArray("features");
        for (int i = 0; i < jsonArray.length(); ++i) {

            OMElement geometry = factory.createOMElement("geometry", feature);
            geometry.addChild(getGeometry(jsonArray.getJSONObject(i).getJSONObject("geometry")));
            myPlaces.addChild(geometry);

            for (String property : FEATURES_LIST) {
                myPlaces.addChild(getElement(jsonArray.getJSONObject(i).getJSONObject("properties"), property, feature));
            }

            myPlaces.addChild(buildIdFilter(jsonArray.getJSONObject(i).getString("feature_id")));
        }
        transaction.addChild(myPlaces);
        root.addChild(transaction);

        return root;
    }

    public OMElement buildFeaturesDelete(String featureId) throws Exception {

        OMElement root = buildWFSRootNode("Transaction", VERSION_1_0_0);

        OMNamespace feature = factory.createOMNamespace("http://www.oskari.org", "feature");
        OMElement transaction = factory.createOMElement("Delete", feature);
        OMAttribute typeName = factory.createOMAttribute("typeName", null, "feature:my_places");
        transaction.addAttribute(typeName);

        transaction.addChild(buildIdFilter(featureId));

        root.addChild(transaction);

        return root;
    }

    private OMElement buildGet(String uuid, String from, String version) throws Exception {

        OMElement root = buildWFSRootNode("GetFeature", version);

        OMElement query = factory.createOMElement("Query", wfsNameSpace);
        OMAttribute typeName = factory.createOMAttribute("typeName", null, from);
        OMAttribute srsName = factory.createOMAttribute("srsName", null, "EPSG:3067");
        query.addAttribute(typeName);
        query.addAttribute(srsName);

        OMNamespace ogc = factory.createOMNamespace("http://www.opengis.net/ogc", "ogc");
        OMElement filter = factory.createOMElement("Filter", ogc);

        OMElement propertyIsEqualTo = factory.createOMElement("PropertyIsEqualTo", ogc);

        //OMAttribute matchCase = factory.createOMAttribute("matchCase", null, "true");
        //propertyIsEqualTo.addAttribute(matchCase);

        OMElement property = factory.createOMElement("PropertyName", ogc);
        property.setText("uuid");
        propertyIsEqualTo.addChild(property);

        OMElement literal = factory.createOMElement("Literal", ogc);
        literal.setText(uuid);
        propertyIsEqualTo.addChild(literal);

        filter.addChild(propertyIsEqualTo);
        query.addChild(filter);
        root.addChild(query);

        return root;
    }

    private OMElement buildWFSRootNode (String wfsType, String version) throws Exception {
        OMElement root = factory.createOMElement(wfsType, wfsNameSpace);
        OMAttribute schemaLocation = factory.createOMAttribute("schemaLocation",
                xmlSchemaInstance,
                "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/"
                        + version + "/wfs.xsd");

        OMAttribute versionElement = factory.createOMAttribute("version", null, version);
        OMAttribute serviceElement = factory.createOMAttribute("service", null, "WFS");

        root.addAttribute(schemaLocation);
        root.addAttribute(versionElement);
        root.addAttribute(serviceElement);

        return root;
    }

    private OMElement getElement(JSONObject jsonObject, String fieldName, OMNamespace feature) throws Exception {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        String value = jsonObject.getString(fieldName);
        OMElement var = factory.createOMElement(fieldName, feature);
        var.setText(value);

        return var;
    }

    private OMElement buildPropertyElement(JSONObject jsonObject, String fieldName, OMNamespace feature) throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement property = null;

        property = factory.createOMElement("Property", feature);

        OMElement propertyName = factory.createOMElement("Name", feature);
        propertyName.setText(fieldName);
        property.addChild(propertyName);

        String value = jsonObject.getString(fieldName);
        OMElement propertyValue = factory.createOMElement("Value", feature);
        propertyValue.setText(value);
        property.addChild(propertyValue);

        return property;
    }

    private OMElement buildIdFilter(String categoryId) {

        OMNamespace ogc = factory.createOMNamespace("http://www.opengis.net/ogc", "ogc");
        OMElement filter = factory.createOMElement("Filter", ogc);

        OMElement property = factory.createOMElement("FeatureId", ogc);
        OMAttribute idAttribute = factory.createOMAttribute("fid", null, categoryId);
        property.addAttribute(idAttribute);

        filter.addChild(property);

        return filter;
    }

    private OMElement getGeometry(JSONObject geometryJson) throws Exception {

        OMNamespace gml = factory.createOMNamespace("http://www.opengis.net/gml", "gml");
        OMElement geometry = factory.createOMElement("Point", gml);

        OMAttribute srsAttribute = factory.createOMAttribute("srsName", null, "EPSG:3067");
        geometry.addAttribute(srsAttribute);

        OMElement coordinates = factory.createOMElement("coordinates", gml);
        OMAttribute decimalAttribute = factory.createOMAttribute("decimal", null, ".");
        coordinates.addAttribute(decimalAttribute);

        OMAttribute csAttribute = factory.createOMAttribute("cs", null, ",");
        coordinates.addAttribute(csAttribute);

        OMAttribute tsAttribute = factory.createOMAttribute("ts", null, " ");
        coordinates.addAttribute(tsAttribute);

        coordinates.setText("387783.46467153,6683374.1552176");

        geometry.addChild(coordinates);

        return geometry;
    }
}
