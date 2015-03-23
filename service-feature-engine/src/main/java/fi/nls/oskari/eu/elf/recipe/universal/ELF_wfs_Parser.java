package fi.nls.oskari.eu.elf.recipe.universal;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.*;

public class ELF_wfs_Parser extends GML32  {




    public void parse() throws IOException {


        final QName scanQN = this.parseWorker.getScanQN();
        final QName rootQN = this.parseWorker.getRootQN();

        final FeatureOutputContext outputContext = new FeatureOutputContext(rootQN);

        // Attribute and element mappings
        Map<String, String> attrmap = new HashMap<String, String>();
        Map<String, String> elemmap = new HashMap<String, String>();
        Map<String, String> typemap = new HashMap<String, String>();
        Map<String, String> nilmap = new HashMap<String, String>();
        Map<String, Resource> resmap = new HashMap<String, Resource>();
        Resource hrefRes = null;

        this.parseWorker.setupMaps(attrmap, elemmap, typemap, nilmap);
        // Output resources
        JSONArray conf = JSONHelper.getJSONArray(this.parseWorker.parseConfig,"paths");
        for (int i = 0; i < conf.length(); i++) {
            JSONObject item = conf.optJSONObject(i);

            //Not id
            if (JSONHelper.getStringFromJSON(item, "label", "unknown").equals("id")) continue;
            final Resource resource = outputContext
                    .addOutputStringProperty(JSONHelper.getStringFromJSON(item, "label", "unknown"));

            resmap.put(JSONHelper.getStringFromJSON(item, "label", "unknown"), resource);
            if (JSONHelper.getStringFromJSON(item, "type", "String").equals("Href")) hrefRes = resource;

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
                switch(xsr.nextTag()) {
                    case XMLStreamConstants.START_ELEMENT:

                        JSONObject feature = new JSONObject();
                        JSONObject additionalFea = new JSONObject();
                        Geometry ggeom = null;
                        QName qn = xsr.getName();
                        if(qn.getLocalPart().equals("additionalObjects")) isAdditional = true;

                        String textTag = null;
                        Object subfea = null;
                        List pathTag = new ArrayList();

                      //Loop all childrens of root element
                        boolean isMore = true;
                       while (isMore) {
                           xsr.next();

                           if(xsr.getEventType() == XMLStreamReader.START_ELEMENT){
                               QName tt = xsr.getName();
                               pathTag.add("/" +tt.getPrefix()+":"+tt.getLocalPart());

                               if(this.getGeometryDeserializer().getHandlers().get(tt) != null){
                                      // Any geometry is parsered
                                      //TODO: parse geometry under mapped element
                                       ggeom = (Geometry) this.getGeometryDeserializer().parseGeometry(this.getGeometryDeserializer().getHandlers(), rootQN, xsr);


                               }
                               else
                               {
                                   String elem = elemmap.get(this.parseWorker.getPathString(pathTag));
                                   String type = typemap.get(this.parseWorker.getPathString(pathTag));
                                   for (int i=0; i <xsr.getAttributeCount(); i++){
                                      String label = attrmap.get(this.parseWorker.getPathString(pathTag)+"/"+xsr.getAttributePrefix(i)+":"+xsr.getAttributeLocalName(i));
                                       if(label != null){

                                           if(isAdditional){
                                               // store current fea, if new member gml:id and prepare new one
                                               //TODO: make better parsing of addtionalFeatures
                                               if((xsr.getAttributePrefix(i)+":"+xsr.getAttributeLocalName(i)).equals("gml:id") && !additionalFea.toString().equals("{}"))
                                               {
                                                   additionalFeas.add(additionalFea);
                                                   additionalFea = new JSONObject();

                                               }
                                               additionalFea.accumulate(label, xsr.getAttributeValue(i));
                                           }
                                           else feature.accumulate(label, xsr.getAttributeValue(i));
                                       }
                                       if(elem != null && xsr.getAttributeLocalName(i).equals("nilReason") ){
                                           nilmap.put(elem, xsr.getAttributeValue(i));
                                       }
                                   }
                                   if(elem != null && type != null && type.equals("Object") ){
                                       // Parse as Object
                                       subfea =   this.getMapper().readValue(xsr,Object.class);

                                   }

                               }


                           }
                           else if (xsr.getEventType() == XMLStreamReader.CHARACTERS)
                           {
                               if(xsr.hasText()) textTag = xsr.getText().trim();
                           }
                           else if (xsr.getEventType() == XMLStreamReader.END_ELEMENT)
                           {
                               QName tt = xsr.getName();
                               String elem = elemmap.get(this.parseWorker.getPathString(pathTag));
                               String type = typemap.get(this.parseWorker.getPathString(pathTag));
                               if(elem != null && (textTag == null || textTag.isEmpty())){
                                   //Add nilreason
                                   textTag = nilmap.get(elem);
                               }
                               if(elem != null && textTag != null && !textTag.isEmpty()){


                                   if(isAdditional)additionalFea.accumulate(elem, textTag);
                                   else feature.accumulate(elem, textTag);
                                   textTag = null;

                               }
                               else if (elem != null && type != null && type.equals("Object") && subfea != null){
                                   if(isAdditional)additionalFea.accumulate(elem, subfea);
                                   else feature.accumulate(elem, subfea);
                                   subfea = null;

                               }

                               this.parseWorker.unStepPathTag(pathTag,tt);

                               if (tt.getLocalPart().equals(rootQN.getLocalPart())) {
                                   isMore = false;
                               }
                           }
                           else if (xsr.getEventType() == XMLStreamReader.END_DOCUMENT)
                           {

                               isMore = false;
                           }
                           else
                           {
                              // Other events nop

                           }

                       }
                        if(!feature.toString().equals("{}")) {
                            Resource output_ID = null;
                            if (feature.has("id")){
                                output_ID = outputContext.uniqueId(feature.getString("id"));
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
                                    if (!key.equals("id")){
                                        Object prop = feature.get(key);
                                        if(prop instanceof JSONArray) prop = JSONHelper.getArrayAsList((JSONArray) prop);
                                        if(prop instanceof JSONObject) prop = JSONHelper.getObjectAsMap((JSONObject) prop);
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
                        else if(!additionalFea.toString().equals("{}")) {
                            // href features to List
                            additionalFeas.add(additionalFea);

                        }
                       

                        break;
                    default:

                      // Other events nop

                }



            }

            // Merge href features, if any
            if(additionalFeas.size() > 0)
            {
                this.output.merge(additionalFeas, hrefRes);
            }
        }
        catch (Exception e)
        {
        }







    }



}
