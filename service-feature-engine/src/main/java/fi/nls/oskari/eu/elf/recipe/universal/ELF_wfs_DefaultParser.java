package fi.nls.oskari.eu.elf.recipe.universal;

/**
 * Generic default parser  for WFS 2.0.0 FeatureCollection
 * - default config is in oskari_wfs_parser_config db table
 * - test driver e.g. \service-feature-engine\src\test\java\fi\nls\oskari\eu\elf\addresses\TestJacksonParser.java
 * */

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.generic.FeExceptionChecker;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.*;

public class ELF_wfs_DefaultParser extends GML32 {

    private static final String KEY_PATHS = "paths";
    private static final String KEY_LABEL = "label";
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";

    private static final String TYPE_STRING = "String";
    private static final String TYPE_HREF = "Href";
    private static final String TYPE_OBJECT = "Object";
    private static final String TYPE_GEOMETRY = "Geometry";
    private static final List<String> EXCLUDE_ATTRIBUTES = Arrays.asList("nil");

    private static final String VALUE_UNKNOWN = "unknown";


    protected static final Logger log = LogFactory
            .getLogger(ELF_wfs_DefaultParser.class);

    public void parse() throws IOException {


        final QName scanQN = parseWorker.getScanQN();
        final QName rootQN = parseWorker.getRootQN();

        final FeatureOutputContext outputContext = new FeatureOutputContext(rootQN);

        Map<String, Resource> resmap = new HashMap<String, Resource>();

        Boolean isGeomMapping = false;
        Boolean isResourcesPrepared = false;


        final Resource geom = outputContext.addDefaultGeometryProperty();

        OutputFeature<Object> outputFeature = null;

        final InputFeature<Object> iter = new InputFeature<Object>(
                scanQN, Object.class);
        JSONObject additionalFea = new JSONObject();


        try {
            XMLStreamReader xsr = iter.getStreamReader(scanQN);


            while (xsr.hasNext()) {
                // Handle unexpected end of document
                int nextTag = XMLStreamConstants.END_DOCUMENT;
                try {
                    nextTag = xsr.next();
                } catch (Exception e) {
                    log.debug("*** Unknown next event", e);
                }

                switch (nextTag) {
                    case XMLStreamConstants.START_ELEMENT:

                        JSONObject feature = new JSONObject();
                        additionalFea = new JSONObject();
                        Geometry ggeom = null;
                        QName qn = xsr.getName();

                        // Check, if exception element in response
                        // if yes, TransportJobException is thrown
                        if (FeExceptionChecker.check(qn)){
                            FeExceptionChecker.breakAndThrow(xsr);
                        }

                        // Skip if not member or featureMembers or featureMember
                        if (qn != null && !scanQN.getLocalPart().equals(qn.getLocalPart())) {
                            xsr.next();
                            break;
                        }

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


                                if (!isGeomMapping && this.getGeometryDeserializer().getHandlers().get(curQN) != null && !curQN.getLocalPart().equals("Envelope")) {
                                    // Parse any geometry
                                    ggeom = (Geometry) this.getGeometryDeserializer().parseGeometry(this.getGeometryDeserializer().getHandlers(), rootQN, xsr);
                                } else {
                                    // Attributes are in start element
                                    for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                        String label = xsr.getAttributeLocalName(i);
                                        if (label != null) {
                                            // jump over common xml attributes
                                            if(EXCLUDE_ATTRIBUTES.contains(label)) continue;

                                            // id is special case
                                            if(label.equals(KEY_ID) && !feature.has(KEY_ID)){
                                                feature.put(label, xsr.getAttributeValue(i));
                                            }
                                            else {
                                                feature.accumulate(curQN.getLocalPart() + "_" + label, xsr.getAttributeValue(i));
                                            }
                                        }

                                    }

                                    // Parse as Object
                                    // subfea = this.getMapper().readValue(xsr, Object.class);


                                }


                            } else if (xsr.getEventType() == XMLStreamReader.CHARACTERS) {
                                if (xsr.hasText()) textTag = xsr.getText().trim();
                                if (textTag.toUpperCase().indexOf("EXCEPTION") > -1)
                                    log.debug("Exception in response: ", textTag);
                            } else if (xsr.getEventType() == XMLStreamReader.END_ELEMENT) {
                                qn = xsr.getName();
                                String elem = parseWorker.getGenericName(pathTag);

                                if (elem != null && textTag != null && !textTag.isEmpty()) {
                                    feature.accumulate(elem, textTag);
                                    textTag = null;

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
                            if (!isResourcesPrepared) {
                                // Output resources for output processor
                                Iterator<?> keys = feature.keys();

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    final Resource resource = outputContext
                                            .addOutputStringProperty(key);

                                    resmap.put(key, resource);

                                }

                                outputContext.build();

                                outputFeature = new OutputFeature<Object>(
                                        outputContext);
                                isResourcesPrepared = true;
                            }


                            Resource output_ID = null;
                            if (feature.has(KEY_ID)) {
                                output_ID = outputContext.uniqueId(feature.getString(KEY_ID));
                                outputFeature.setFeature(new Object()).setId(output_ID);
                            }

                            // To FE feature
                            //TODO: property type mapping
                            Iterator<?> keys = feature.keys();

                            while (keys.hasNext()) {
                                String key = (String) keys.next();
                                Resource res = resmap.get(key);
                                if (res != null) {
                                    // Get id when it is the only attribute
                                    if (!key.equals(KEY_ID) || feature.length() == 1) {
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
                        }
                        break;

                    default:

                        // Other events nop


                }


            }


        }catch (ServiceRuntimeException e) {
            log.debug("*** default path parsing failed - ", e);
            throw new ServiceRuntimeException(e.getMessage(),e.getMessageKey());
        }
        catch (Exception e) {
            log.debug("*** default path parsing failed - ", e);
            throw new ServiceRuntimeException(e.getMessage());
        }


    }


}
