package fi.nls.oskari.eu.elf.recipe.universal;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.*;

public class ELF_wfs_Parser extends GML32 {

    private static final String KEY_PATHS = "paths";
    private static final String KEY_LABEL = "label";
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";

    private static final String TYPE_STRING = "String";
    private static final String TYPE_HREF = "Href";
    private static final String TYPE_OBJECT = "Object";
    private static final String TYPE_GEOMETRY = "Geometry";

    private static final String VALUE_NILREASON = "nilReason";
    private static final String VALUE_UNKNOWN = "unknown";

    private static final String ELEM_ADDITIONALOBJECTS = "additionalObjects";

    protected static final Logger log = LogFactory
            .getLogger(ELF_wfs_Parser.class);

    public void parse() throws IOException {


        final QName scanQN = parseWorker.getScanQN();
        final QName rootQN = parseWorker.getRootQN();

        final FeatureOutputContext outputContext = new FeatureOutputContext(rootQN);

        // Attribute and element mappings
        Map<String, String> attrmap = new HashMap<String, String>();
        Map<String, String> elemmap = new HashMap<String, String>();
        Map<String, String> typemap = new HashMap<String, String>();
        Map<String, String> nilmap = new HashMap<String, String>();
        Map<String, Resource> resmap = new HashMap<String, Resource>();
        Resource hrefRes = null;

        parseWorker.setupMaps(attrmap, elemmap, typemap, nilmap);
        // Output resources
        JSONArray conf = JSONHelper.getJSONArray(parseWorker.parseConfig, KEY_PATHS);
        for (int i = 0; i < conf.length(); i++) {
            JSONObject item = conf.optJSONObject(i);

            //Not id
            if (JSONHelper.getStringFromJSON(item, KEY_LABEL, VALUE_UNKNOWN).equals(KEY_ID)) continue;
            final Resource resource = outputContext
                    .addOutputStringProperty(JSONHelper.getStringFromJSON(item, KEY_LABEL, VALUE_UNKNOWN));

            resmap.put(JSONHelper.getStringFromJSON(item, KEY_LABEL, VALUE_UNKNOWN), resource);
            if (JSONHelper.getStringFromJSON(item, KEY_TYPE, TYPE_STRING).equals(TYPE_HREF)) hrefRes = resource;

        }


        final Resource geom = outputContext.addDefaultGeometryProperty();


        outputContext.build();

        final OutputFeature<Object> outputFeature = new OutputFeature<Object>(
                outputContext);

        final InputFeature<Object> iter = new InputFeature<Object>(
                scanQN, Object.class);


        try {
            XMLStreamReader xsr = iter.getStreamReader(scanQN);

            boolean isAdditional = false;  // Is there addtional object in stream bottom
            List<JSONObject> additionalFeas = new ArrayList<JSONObject>();

            while (xsr.hasNext()) {
                switch (xsr.nextTag()) {
                    case XMLStreamConstants.START_ELEMENT:

                        JSONObject feature = new JSONObject();
                        JSONObject additionalFea = new JSONObject();
                        Geometry ggeom = null;
                        QName qn = xsr.getName();
                        // There are local href elements  inside this element - put flag on
                        if (qn.getLocalPart().equals(ELEM_ADDITIONALOBJECTS)) isAdditional = true;

                        String textTag = null;
                        Object subfea = null;
                        List pathTag = new ArrayList();

                        //Loop all childrens of root element
                        boolean isRootOpen = true;
                        while (isRootOpen) {
                            xsr.next();

                            if (xsr.getEventType() == XMLStreamReader.START_ELEMENT) {
                                QName curQN = xsr.getName();
                                parseWorker.addPathTag(pathTag, rootQN, curQN);

                                String elem = elemmap.get(parseWorker.getPathString(pathTag));
                                String type = typemap.get(parseWorker.getPathString(pathTag));

                                if (elem != null && type != null && type.equals(TYPE_GEOMETRY)) {  // <--- parse mapped geometry
                                // if (this.getGeometryDeserializer().getHandlers().get(curQN) != null) {  <-- parse any geometry
                                    ggeom = (Geometry) this.getGeometryDeserializer().parseGeometry(this.getGeometryDeserializer().getHandlers(), rootQN, xsr);

                                } else {
                                   // Attributes are in start element
                                    for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                        String label = attrmap.get(parseWorker.getPathString(pathTag) + "/" + xsr.getAttributePrefix(i) + ":" + xsr.getAttributeLocalName(i));
                                        if (label != null) {

                                            if (isAdditional) {
                                                // store current fea, if new member gml:id and prepare new one
                                                //TODO: make better parsing of addtionalFeatures
                                                if ((xsr.getAttributePrefix(i) + ":" + xsr.getAttributeLocalName(i)).equals("gml:id") && !additionalFea.toString().equals("{}")) {
                                                    additionalFeas.add(additionalFea);
                                                    additionalFea = new JSONObject();

                                                }
                                                additionalFea.accumulate(label, xsr.getAttributeValue(i));
                                            } else feature.accumulate(label, xsr.getAttributeValue(i));
                                        }
                                        if (elem != null && xsr.getAttributeLocalName(i).equals(VALUE_NILREASON)) {
                                            nilmap.put(elem, xsr.getAttributeValue(i));
                                        }
                                    }
                                    if (elem != null && type != null && type.equals(TYPE_OBJECT)) {
                                        // Parse as Object
                                        subfea = this.getMapper().readValue(xsr, Object.class);

                                    }

                                }


                            } else if (xsr.getEventType() == XMLStreamReader.CHARACTERS) {
                                if (xsr.hasText()) textTag = xsr.getText().trim();
                            } else if (xsr.getEventType() == XMLStreamReader.END_ELEMENT) {
                                qn = xsr.getName();
                                String elem = elemmap.get(parseWorker.getPathString(pathTag));
                                String type = typemap.get(parseWorker.getPathString(pathTag));
                                if (elem != null && (textTag == null || textTag.isEmpty())) {
                                    //Add nilreason
                                    textTag = nilmap.get(elem);
                                }
                                if (elem != null && textTag != null && !textTag.isEmpty()) {


                                    if (isAdditional) additionalFea.accumulate(elem, textTag);
                                    else feature.accumulate(elem, textTag);
                                    textTag = null;

                                } else if (elem != null && type != null && type.equals(TYPE_OBJECT) && subfea != null) {
                                    if (isAdditional) additionalFea.accumulate(elem, subfea);
                                    else feature.accumulate(elem, subfea);
                                    subfea = null;

                                }

                                parseWorker.unStepPathTag(pathTag, qn);

                                if (qn.getLocalPart().equals(rootQN.getLocalPart())) {
                                    isRootOpen = false;
                                }
                            } else if (xsr.getEventType() == XMLStreamReader.END_DOCUMENT) {

                                isRootOpen = false;
                            } else {
                                // Other events nop

                            }

                        }
                        if (!feature.toString().equals("{}")) {
                            Resource output_ID = null;
                            if (feature.has(KEY_ID)) {
                                output_ID = outputContext.uniqueId(feature.getString(KEY_ID));
                                outputFeature.setFeature(new Object()).setId(output_ID);
                            }

                            // To FE feature
                            Object fea = new Object();

                            //TODO: property type mapping
                            Iterator<?> keys = feature.keys();

                            while (keys.hasNext()) {
                                String key = (String) keys.next();
                                Resource res = resmap.get(key);
                                if (res != null) {
                                    if (!key.equals(KEY_ID)) {
                                        Object prop = feature.get(key);
                                        if (prop instanceof JSONArray)
                                            prop = JSONHelper.getArrayAsList((JSONArray) prop);
                                        if (prop instanceof JSONObject)
                                            prop = JSONHelper.getObjectAsMap((JSONObject) prop);
                                        outputFeature.addProperty(res, prop);
                                    }
                                }
                            }

                            // outputFeature.addProperty(obj, feature.toString());
                            if (ggeom != null) {
                                outputFeature
                                        .addGeometryProperty(
                                                geom,
                                                ggeom);
                            }


                            outputFeature.build();
                        } else if (!additionalFea.toString().equals("{}")) {
                            // href features to List
                            additionalFeas.add(additionalFea);

                        }


                        break;
                    default:

                        // Other events nop

                }


            }

            // Merge href features, if any
            if (additionalFeas.size() > 0) {
                this.output.merge(additionalFeas, hrefRes);
            }
        } catch (Exception e) {
            log.debug("*** path parsing failed - ", e);

        }


    }


}
