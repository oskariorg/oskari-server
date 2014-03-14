package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.io.*;
import java.util.*;

import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.*;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.v1_0_0.WFSFeatureStore;
import org.geotools.data.wfs.v1_0_0.WFSTransactionState;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;


public class UserLayerDataService {
    private WFSDataStoreFactory dsf;
    private Map<String, Serializable> params;
    private Logger log = LogFactory.getLogger(UserLayerDataService.class);

    public void testwfs() {


        try

        {
            dsf = new WFSDataStoreFactory();
            params = new HashMap<String, Serializable>();
            params.put(WFSDataStoreFactory.URL.key,
                    "http://dev.paikkatietoikkuna.fi/geoserver/oskari/ows?request=GetCapabilities");

            params.put(WFSDataStoreFactory.USERNAME.key, "liferay");

            params.put(WFSDataStoreFactory.PASSWORD.key, "pationus");


            // Step 2 - connection
            DataStore data = DataStoreFinder.getDataStore(params);
            // WFSDataStore dataStore = dsf.createDataStore(params);

            // Step 3 - discouvery
            String typeNames[] = data.getTypeNames();
            String typeName = typeNames[0];
            SimpleFeatureType schema = data.getSchema(typeName);

            // Step 4 - target
            FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource(typeName);
            System.out.println("Metadata Bounds:" + source.getBounds());

            // Step 5 - query
            String geomName = schema.getGeometryDescriptor().getLocalName();
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
            Filter idFilter = ff.equal(ff.property("layer_id"), ff.literal(1), false);
           // Filter uidFilter = ff.equal(ff.property(layerUidField), ff.literal(uid), false);
           // Filtter combi = ff.and(idFilter, uidFilter);


           // Object polygon = JTS.toGeometry(bbox);
            //Intersects filter = ff.intersects(ff.property(geomName), ff.literal(polygon));

            Query query = new Query(typeName, idFilter);
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(query);

            ReferencedEnvelope bounds = new ReferencedEnvelope();
            FeatureIterator<SimpleFeature> iterator = features.features();
            try

            {
                while (iterator.hasNext()) {
                    Feature feature = (Feature) iterator.next();
                    bounds.include(feature.getBounds());
                }
                System.out.println("Calculated Bounds:" + bounds);
            } finally

            {
                features.features().close();
            }
        } catch (Exception e) {
            log.error("Couldn't get wfs features",
                    e);
        }
    }
    public void doWfsInsert(JSONObject geoJson) {

        try

        {
            dsf = new WFSDataStoreFactory();
            params = new HashMap<String, Serializable>();
            params.clear();
            params.put(WFSDataStoreFactory.URL.key,
                    "http://dev.paikkatietoikkuna.fi/geoserver/oskari/ows?request=GetCapabilities&service=wfs&version=1.0.0");

            params.put(WFSDataStoreFactory.USERNAME.key, "liferay");

            params.put(WFSDataStoreFactory.PASSWORD.key, "liferay");


            // Step 1 - wfs connection
            DataStore ds = DataStoreFinder.getDataStore(params);

            // Step 2 - target
            String typeName = "oskari:user_layer_data";

            SimpleFeatureType schema = ds.getSchema(typeName);

            Transaction t = new DefaultTransaction();
            WFSFeatureStore fs = (WFSFeatureStore) ds.getFeatureSource(typeName);
            fs.setTransaction(t);

            // Step 3 - input
            FeatureJSON jsonio = new FeatureJSON();
            //jsonio.setFeatureType(schema);

            JSONArray geofeas = geoJson.getJSONArray("features");
            DefaultFeatureCollection  fc = new DefaultFeatureCollection();

            // Loop json features and fix to user_layer_data structure
            for(int i = 0; i < geofeas.length(); i++)
            {
                JSONObject geofea= geofeas.getJSONObject(i);

                // Fix fea properties  (user_layer_id, uuid, property_json, feature_id
                JSONObject userproperties = new JSONObject();
                JSONHelper.putValue(userproperties,"user_layer_id",1);
                JSONHelper.putValue(userproperties,"uuid",0);
                JSONHelper.putValue(userproperties, "feature_id", geofea.optString("id", ""));
                //JSONHelper.putValue(userproperties,"properties",JSONHelper.getStringFromJSON(geofea.getJSONObject("properties"), "{}"));
                //JSONHelper.putValue(userproperties,"properties","test");

                geofea.put("properties", userproperties);
                // Create feature and add to
                SimpleFeature f2 = jsonio.readFeature(geofea.toString());
                fc.add(f2);

            }

            // Step 4 - featurecollection for to store

            FeatureCollection test = jsonio.readFeatureCollection(geoJson.toString());


            fs.addFeatures(fc.reader());


            t.commit();




            WFSTransactionState ts = (WFSTransactionState) t.getState(ds);

            String[] fids = ts.getFids(typeName);

               int tt=0;

        } catch (Exception e) {
            log.error("Couldn't get wfs features",
                    e);
        }
    }
}
