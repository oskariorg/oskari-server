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

    private static final List<String> LAYERS_GET_LIST = Arrays.asList("category_name", "default", "stroke_width",
            "stroke_color", "fill_color", "uuid", "dot_color", "dot_size", "border_width", "border_color",
            "dot_shape", "stroke_linejoin", "fill_pattern", "stroke_linecap", "stroke_dasharray", "border_linejoin",
            "border_dasharray");

    public OMElement buildLayersGet(String uuid) {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMNamespace xsi = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        OMNamespace wfs = factory.createOMNamespace("http://www.opengis.net/wfs", "wfs");

        OMElement root = factory.createOMElement("GetFeature", wfs);
        try {
            OMAttribute schemaLocation = factory.createOMAttribute("schemaLocation",
                    xsi,
                    "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/"
                            + VERSION_1_1_0 + "/wfs.xsd");

            OMAttribute version = factory.createOMAttribute("version", null, VERSION_1_1_0);
            OMAttribute service = factory.createOMAttribute("service", null, "WFS");

            root.addAttribute(schemaLocation);
            root.addAttribute(version);
            root.addAttribute(service);

            OMElement query = factory.createOMElement("Query", wfs);
            OMAttribute typeName = factory.createOMAttribute("typeName", null, "feature:categories");
            OMAttribute srsName = factory.createOMAttribute("srsName", null, "EPSG:3067");
            query.addAttribute(typeName);
            query.addAttribute(srsName);

            OMNamespace ogc = factory.createOMNamespace("http://www.opengis.net/ogc", "ogc");
            OMElement filter = factory.createOMElement("Filter", ogc);

            OMElement propertyIsEqualTo = factory.createOMElement("PropertyIsEqualTo", ogc);

            OMAttribute matchCase = factory.createOMAttribute("matchCase", null, "true");
            propertyIsEqualTo.addAttribute(matchCase);

            OMElement property = factory.createOMElement("PropertyName", ogc);
            property.setText("uuid");
            propertyIsEqualTo.addChild(property);

            OMElement literal = factory.createOMElement("Literal", ogc);
            literal.setText(uuid);
            propertyIsEqualTo.addChild(literal);

            filter.addChild(propertyIsEqualTo);
            query.addChild(filter);
            root.addChild(query);
        }
        catch (Exception e){
            log.error(e, "Failed to create payload - root: ", root);
            throw new RuntimeException(e.getMessage());
        }

        return root;
    }

    /*

     */
    public OMElement buildLayersInsert(String payload) {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMNamespace xsi = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        OMNamespace wfs = factory.createOMNamespace("http://www.opengis.net/wfs", "wfs");

        OMElement root = factory.createOMElement("Transaction", wfs);
        try {
            OMAttribute schemaLocation = factory.createOMAttribute("schemaLocation",
                    xsi,
                    "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/"
                            + VERSION_1_1_0 + "/wfs.xsd");

            OMAttribute version = factory.createOMAttribute("version", null, VERSION_1_1_0);
            OMAttribute service = factory.createOMAttribute("service", null, "WFS");

            root.addAttribute(schemaLocation);
            root.addAttribute(version);
            root.addAttribute(service);

            OMElement transaction = factory.createOMElement("Insert", wfs);
            OMNamespace feature = factory.createOMNamespace("http://www.oskari.org", "feature");

            OMElement categories = factory.createOMElement("categories", feature);

            try {
                JSONArray jsonArray = new JSONObject(payload).getJSONArray("categories");
                for (int i=0; i<jsonArray.length(); ++i) {
                    for (String property : LAYERS_GET_LIST) {
                        categories.addChild(getElement(jsonArray.getJSONObject(i), property, feature));
                    }
                }
            }
            catch (Exception e) {
                log.error(e, "Failed to read payload json - payload: ", payload);
                throw new RuntimeException(e.getMessage());
            }

            transaction.addChild(categories);
            root.addChild(transaction);
        }
        catch (Exception e){
            log.error(e, "Failed to create payload - root: ", root);
            throw new RuntimeException(e.getMessage());
        }
        return root;
    }

    private OMElement getElement(JSONObject jsonObject, String fieldName, OMNamespace feature) throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement var = null;
        try {
            String value = jsonObject.getString(fieldName);
            var = factory.createOMElement(fieldName, feature);
            var.setText(value);
        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return var;
    }

    public OMElement buildLayersUpdate(String payload) {
        return null;
    }

    public OMElement buildLayersDelete(String payload) {
        return null;
    }

    public OMElement buildFeaturesGet(String payload) {
        return null;
    }

    public OMElement buildFeaturesInsert(String payload) {
        return null;
    }

    public OMElement buildFeaturesUpdate(String payload) {
        return null;
    }

    public OMElement buildFeaturesDelete(String payload) {
        return null;
    }
}
