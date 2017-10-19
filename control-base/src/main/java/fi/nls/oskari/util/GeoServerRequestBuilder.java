package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.wfs.WFSExceptionHelper;
import fi.nls.oskari.wfs.WFSFilterBuilder;
import fi.nls.oskari.wfs.util.XMLHelper;
import org.apache.axiom.om.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GeoServerRequestBuilder {

    private static final Logger log = LogFactory.getLogger(GeoServerRequestBuilder.class);

    private static final String VERSION_1_0_0 = "1.0.0";
    private static final String VERSION_1_1_0 = "1.1.0";

    public OMElement buildLayersGet(String payload) {

        String uuid = null;
        try {
            JSONObject jsonObject = new JSONObject(payload);
            uuid = jsonObject.getString("uuid");
        }
        catch (Exception e) {
            log.error(e, "Failed to read payload json - payload: ", payload);
            throw new RuntimeException(e.getMessage());
        }

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMNamespace xsi = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        OMNamespace wfs = factory.createOMNamespace("http://www.opengis.net/wfs", "wfs");

        OMElement root = factory.createOMElement("GetFeature", wfs);
        try {
            OMAttribute schemaLocation = factory.createOMAttribute("schemaLocation",
                    xsi,
                    "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/"
                            + VERSION_1_0_0 + "/wfs.xsd");

            OMAttribute version = factory.createOMAttribute("version", null, VERSION_1_0_0);
            OMAttribute service = factory.createOMAttribute("service", null, "WFS");

            root.addAttribute(schemaLocation);
            root.addAttribute(version);
            root.addAttribute(service);

            OMElement query = factory.createOMElement("Query", wfs);
            OMAttribute typeName = factory.createOMAttribute("typeName", null, "feature:categories");
            OMAttribute srsName = factory.createOMAttribute("srsName", null, "ESPG:3067");
            query.addAttribute(typeName);
            query.addAttribute(srsName);
            root.addChild(query);

            OMNamespace ogc = factory.createOMNamespace("http://www.opengis.net/wfs", "wfs");
            OMElement filter = factory.createOMElement("Filter", ogc);

            OMElement propertyIsEqualTo = factory.createOMElement("PropertyIsEqualTo", ogc);

            OMAttribute matchCase = factory.createOMAttribute("matchCase", null, "true");
            propertyIsEqualTo.addAttribute(matchCase);
            filter.addChild(propertyIsEqualTo);

            OMElement property = factory.createOMElement("PropertyName", ogc);
            property.setText("uuid");
            propertyIsEqualTo.addChild(property);

            OMElement literal = factory.createOMElement("Literal", ogc);
            property.setText(uuid);
            propertyIsEqualTo.addChild(literal);

            root.addChild(filter);
        }
        catch (Exception e){
            log.error(e, "Failed to create payload - root: ", root);
            throw new RuntimeException(e.getMessage());
        }

        return root;
    }

    public OMElement buildLayersInsert(String payload) {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMNamespace xsi = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        OMNamespace wfs = factory.createOMNamespace("http://www.opengis.net/wfs", "wfs");

        OMElement root = factory.createOMElement("Transaction", wfs);
        try {
            OMAttribute schemaLocation = factory.createOMAttribute("schemaLocation",
                    xsi,
                    "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/"
                            + VERSION_1_0_0 + "/wfs.xsd");

            OMAttribute version = factory.createOMAttribute("version", null, VERSION_1_0_0);
            OMAttribute service = factory.createOMAttribute("service", null, "WFS");

            root.addAttribute(schemaLocation);
            root.addAttribute(version);
            root.addAttribute(service);

            OMElement transaction = factory.createOMElement("Insert", wfs);

            OMElement categories = factory.createOMElement("categories", wfs);
            OMNamespace feature = factory.createOMNamespace("http://www.oskari.org", "feature");

            List<String> propertyList = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(payload);
                for (int i=0; i<jsonArray.length(); ++i) {
                    JSONObject jsonObject = new JSONObject(jsonArray.getJSONObject(i));
                    transaction.addChild(getElement(jsonObject, "category_name", feature));
                    transaction.addChild(getElement(jsonObject, "default", feature));
                    transaction.addChild(getElement(jsonObject, "stroke_width", feature));
                    transaction.addChild(getElement(jsonObject, "stroke_dasharray", feature));
                    transaction.addChild(getElement(jsonObject, "stroke_linecap", feature));
                    transaction.addChild(getElement(jsonObject, "stroke_linejoin", feature));
                    transaction.addChild(getElement(jsonObject, "stroke_color", feature));
                    transaction.addChild(getElement(jsonObject, "border_width", feature));
                    transaction.addChild(getElement(jsonObject, "border_dasharray", feature));
                    transaction.addChild(getElement(jsonObject, "border_linejoin", feature));
                    transaction.addChild(getElement(jsonObject, "border_color", feature));
                    transaction.addChild(getElement(jsonObject, "fill_color", feature));
                    transaction.addChild(getElement(jsonObject, "fill_pattern", feature));
                    transaction.addChild(getElement(jsonObject, "dot_color", feature));
                    transaction.addChild(getElement(jsonObject, "dot_size", feature));
                    transaction.addChild(getElement(jsonObject, "dot_shape", feature));
                    transaction.addChild(getElement(jsonObject, "uuid", feature));
                }
            }
            catch (Exception e) {
                log.error(e, "Failed to read payload json - payload: ", payload);
                throw new RuntimeException(e.getMessage());
            }

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
