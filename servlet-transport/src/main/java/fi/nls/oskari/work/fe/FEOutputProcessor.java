package fi.nls.oskari.work.fe;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;
import java.util.*;

public class FEOutputProcessor implements OutputProcessor {
    protected static final Logger log = LogFactory
            .getLogger(FEOutputProcessor.class);

    final Map<Resource, SimpleFeatureBuilder> responseBuilders = new HashMap<Resource, SimpleFeatureBuilder>();
    final Map<Resource, List<SimpleFeature>> responseFeatures = new HashMap<Resource, List<SimpleFeature>>();
    final ArrayList<List<Object>> list;
    final Map<Resource, SimpleFeatureCollection> responseCollections;

    final CoordinateReferenceSystem crs;

    final FERequestResponse requestResponse;

    final ArrayList<String> selectedProperties;

    final Map<Resource, Integer> selectedPropertiesIndex;

    final MathTransform transform;
    final String geomProp;

    public FEOutputProcessor(final ArrayList<List<Object>> list,
            final Map<Resource, SimpleFeatureCollection> responseCollections,
            CoordinateReferenceSystem crs, FERequestResponse requestResponse,
            ArrayList<String> selectedProperties,
            Map<Resource, Integer> selectedPropertiesIndex,
            MathTransform transform,
            String geomProp) {
        this.list = list;
        this.responseCollections = responseCollections;
        this.crs = crs;
        this.requestResponse = requestResponse;
        this.selectedProperties = selectedProperties;
        this.selectedPropertiesIndex = selectedPropertiesIndex;
        this.transform = transform;
        this.geomProp = geomProp;
    }

    public void begin() throws IOException {
        /* Setup MAP */

    }

    @Override
    public void edge(Resource subject, Resource predicate, Resource value)
            throws IOException {
    }

    @Override
    public void end() throws IOException {

        for (Resource type : responseFeatures.keySet()) {
            List<SimpleFeature> sfc = getAndSetListSimpleFeature(type);

            SimpleFeatureCollection fc = DataUtilities.collection(sfc);

            responseCollections.put(type, fc);

            log.debug("[fe] type: " + type + " / fc: { len: " + fc.size() + "}");
        }
    }

    @Override
    public void flush() throws IOException {
    }

    public SimpleFeatureBuilder getAndSetFeatureBuilder(Resource type, List<Pair<Resource, Object>> simpleProperties) {

        SimpleFeatureBuilder sfb = responseBuilders.get(type);
        if (sfb == null) {

            SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
            ftb.setName(type.getLocalPart());
            ftb.setNamespaceURI(type.getNs());

            // add a geometry property

            ftb.setCRS(crs); // set crs first
            // then add geometry
            ftb.add(this.geomProp, Geometry.class, crs);

            // Add other properties
            if (selectedProperties != null && selectedProperties.size() > 0) {

                for (Pair<Resource, ?> pair : simpleProperties) {
                    Integer keyIndex = selectedPropertiesIndex.get(pair.getKey());
                    if (keyIndex == null) {
                    /*
                     * log.debug("KEY INDEX FOR " + pair.getKey() +
                     * " not found");
                     */
                        continue;
                    }
                    //TODO: type management
                    ftb.add(pair.getKey().getLocalPart(),  pair.getValue().getClass());

                }
            }

            SimpleFeatureType schema = ftb.buildFeatureType();

            sfb = new SimpleFeatureBuilder(schema);

            responseBuilders.put(type, sfb);

            log.debug("[fe] creating featurebuilder for : " + type);
        }
        return sfb;
    }

    public List<SimpleFeature> getAndSetListSimpleFeature(Resource type) {

        List<SimpleFeature> list = responseFeatures.get(type);
        if (list == null) {
            list = new LinkedList<SimpleFeature>();
            responseFeatures.put(type, list);
            log.debug("[fe] creating featureList for : " + type);
        }
        return list;
    }

    @Override
    public void prefix(String prefix, String ns) throws IOException {
    }

    @Override
    public void type(Resource type,
            List<Pair<Resource, XSDDatatype>> simpleProperties,
            List<Pair<Resource, Object>> linkProperties,
            List<Pair<Resource, String>> geometryProperties) throws IOException {
        requestResponse.setFeatureIri(type);

        log.debug("[fe] registering (generic) output type for " + type);

        /*
         * List<String> layerSelectedProperties = layer
         * .getSelectedFeatureParams(session.getLanguage());
         */
        selectedProperties.add(0, "__fid");
        log.debug("- Property:" + "__fid" + " as 0");
        for (Pair<Resource, XSDDatatype> prop : simpleProperties) {

            log.debug("- Property:" + prop.getKey() + " as "
                    + selectedProperties.size());
            selectedPropertiesIndex.put(prop.getKey(),
                    selectedProperties.size());
            selectedProperties.add(prop.getKey().getLocalPart());

        }
        selectedProperties.add("__centerX");
        log.debug("- Property:" + "__centerX" + " as "+ selectedProperties.size());
        selectedProperties.add("__centerY");
        log.debug("- Property:" + "__centerY" + " as "+ selectedProperties.size());
        
        

    }

    public void vertex(final Resource iri, final Resource type,
            final List<Pair<Resource, Object>> simpleProperties,
            final List<Pair<Resource, Object>> linkProperties)
            throws IOException {
    }

    public void vertex(Resource iri, Resource type,
            List<Pair<Resource, Object>> simpleProperties,
            List<Pair<Resource, Object>> linkProperties,
            List<Pair<Resource, Geometry>> geometryProperties)
            throws IOException {

        SimpleFeatureBuilder sfb = getAndSetFeatureBuilder(type, simpleProperties);
        List<SimpleFeature> sfc = getAndSetListSimpleFeature(type);

        for (Pair<Resource, Geometry> geomPair : geometryProperties) {
            Geometry geom = geomPair.getValue();

            try {
                geom = JTS.transform(geom, transform);
            } catch (MismatchedDimensionException e) {

                throw new IOException(e);
            } catch (TransformException e) {

                throw new IOException(e);
            }

            sfb.add(geom);
            // Add other properties
            if (selectedProperties != null && selectedProperties.size() > 0) {

                for (Pair<Resource, ?> pair : simpleProperties) {
                    Integer keyIndex = selectedPropertiesIndex.get(pair.getKey());
                    if (keyIndex == null) {
                    /*
                     * log.debug("KEY INDEX FOR " + pair.getKey() +
                     * " not found");
                     */
                        continue;
                    }
                    sfb.add(pair.getValue());
                }


            }

            SimpleFeature f = sfb.buildFeature(iri.getUuid());

            sfc.add(f);

        }

        if (!(type.getNs().equals(requestResponse.getFeatureIri().getNs()) && type
                .getLocalPart().equals(
                        requestResponse.getFeatureIri().getLocalPart()))) {
            log.debug("[fe] type mismatch for Transport regd "
                    + requestResponse.getFeatureIri() + " vs added " + type
                    + " -> properties discarded");
            return;
        }

        if (selectedProperties != null && selectedProperties.size() > 0) {
            ArrayList<Object> props = new ArrayList<Object>(
                    selectedProperties.size());
            for (String field : selectedProperties) {
                props.add(null);
            }
            props.set(0, iri.getUuid());  //Use local part for id
            for (Pair<Resource, ?> pair : simpleProperties) {
                Integer keyIndex = selectedPropertiesIndex.get(pair.getKey());
                if (keyIndex == null) {
                    /*
                     * log.debug("KEY INDEX FOR " + pair.getKey() +
                     * " not found");
                     */
                    continue;
                }
                props.set(keyIndex, pair.getValue());
            }

            list.add(props);
        }
    }

    /**
     * Merge href sub features to main features
     *
     * @param jlist addtional features to where local hrefs point
     * @param res   href property resourse
     */
    public void merge(List<JSONObject> jlist, Resource res) {
        // Get index of resource
        Integer keyInd = selectedPropertiesIndex.get(res);
        if (keyInd == null) return;

        try {
            //Loop features
            for (List lis : list) {
                // Href key
                Object val = lis.get(keyInd);
                if (val instanceof List) {
                    ArrayList<String> vallist = (ArrayList<String>) val;
                    List<Map<String, Object>> hrefFeas = new ArrayList<Map<String, Object>>();
                    for (String lval : vallist) {
                        if (lval == null) continue;
                        lval = lval.replace("#", "");
                        for (JSONObject js : jlist) {
                            if (JSONHelper.getStringFromJSON(js, "id", "").equals(lval)) {
                                hrefFeas.add(JSONHelper.getObjectAsMap(js));
                                break;
                            }
                        }

                    }
                    // Replace refs with objects
                    if(hrefFeas.size() > 0) lis.set(keyInd, hrefFeas);

                }
            }

        } catch (Exception ee) {
            log.debug("Local href subfeature merge failed:", ee);
        }
    }

    /**
     * Make property element jsonarrays to equal size
     *
     * @param multiElemmap elements to make equal size

     */
    public void equalizePropertyArraySize(Map<String, Integer> multiElemmap, Map<String, Resource> resmap) {
        if (multiElemmap.size() < 1) return;
        try {
            // loop hashmap
            Resource res = null;
            int maxsize = 0;
            for (Map.Entry<String, Integer> entry : multiElemmap.entrySet()) {
                res = resmap.get(entry.getKey());
                maxsize = entry.getValue();
                // Get index of resource
                Integer keyInd = selectedPropertiesIndex.get(res);
                if (keyInd == null) return;

                //Loop features and equalize size
                for (List lis : list) {
                    // Href key
                    Object val = lis.get(keyInd);
                    if (val instanceof List) {
                        ArrayList<Object> valList = (ArrayList<Object>) val;
                        if(valList.size() < maxsize){
                            for(int i=0; i < (maxsize - valList.size()); i++){
                                valList.add(null);
                            }
                            lis.set(keyInd,valList);
                        }

                    } else {
                        ArrayList<Object> newList = new ArrayList<Object>();
                        newList.add(val);
                        for(int i=0; i < (maxsize - 1); i++){
                            newList.add(null);
                        }
                        lis.set(keyInd,newList);
                    }
                }


            }
        } catch (Exception ee) {
            log.debug("Array size equalizing failed:", ee);
        }


    }


};