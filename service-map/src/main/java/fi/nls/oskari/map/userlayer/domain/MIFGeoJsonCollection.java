package fi.nls.oskari.map.userlayer.domain;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.userlayer.service.GeoJsonWorker;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.DataStore;
import org.geotools.data.ogr.OGRDataStoreFactory;
import org.geotools.data.ogr.bridj.BridjOGRDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import com.vividsolutions.jts.geom.Geometry;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MIFGeoJsonCollection extends GeoJsonCollection implements GeoJsonWorker {

    final FeatureJSON io = new FeatureJSON();
    private static final Logger log = LogFactory
            .getLogger(MIFGeoJsonCollection.class);

    /**
     * Parse MapInfo file set to geojson features
     * Coordinate transformation is executed, if shape .prj file is within
     * @param file .mif import file
     * @param target_epsg target CRS
     * @return
     */
    public boolean parseGeoJSON(File file, String target_epsg) {
        OGRDataStoreFactory factory = new BridjOGRDataStoreFactory();

        Map<String, String> connectionParams = new HashMap<String, String>();
        connectionParams.put("DriverName", "MapInfo File");
        connectionParams.put("DatasourceName", file.getAbsolutePath());
        DataStore store = null;
        String typeName = null;
        SimpleFeatureSource source;
        SimpleFeatureCollection collection;
        SimpleFeatureType schema = null;
        SimpleFeatureIterator it = null;
        CoordinateReferenceSystem sourceCrs = null;
        CoordinateReferenceSystem targetCrs = null;
        MathTransform transform = null;
        JSONArray features = null;
        ReferencedEnvelope bounds = null;

        try {
            store = factory.createDataStore(connectionParams);
            typeName = store.getTypeNames()[0];
            source = store.getFeatureSource(typeName);
            collection = source.getFeatures();
            it = collection.features();
            schema = collection.getSchema();

            //Coordinate transformation support
            bounds = source.getBounds();
            if (bounds != null) {
                sourceCrs = bounds.getCoordinateReferenceSystem();
            }
            if (sourceCrs == null) {
                sourceCrs = schema.getCoordinateReferenceSystem();
            }

            // Oskari crs
            //(oskari OL map crs)
            targetCrs = CRS.decode(target_epsg, true);

            // TODO: better check algorithm - name is not 100% proof
            if ((sourceCrs != null)&&(!targetCrs.getName().equals(sourceCrs.getName()))) {
                transform = CRS.findMathTransform(sourceCrs, targetCrs, true);
            }

            features = new JSONArray();
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                if (transform != null) {
                    Geometry geometry = (Geometry) feature.getDefaultGeometry();
                    feature.setDefaultGeometry(JTS.transform(geometry, transform));
                }
                JSONObject geojs = JSONHelper.createJSONObject(io.toString(feature));
                if (geojs != null) {
                    features.put(geojs);
                }
            }
            it.close();
            setGeoJson(JSONHelper.createJSONObject("features",features));
            setFeatureType((FeatureType)schema);
            setTypeName(typeName);
            return true;
        } catch (Exception e) {
            log.error("Couldn't create geoJSON from the MapInfo file ", file.getName(), e);
            return false;
        }
        finally {
            store.dispose();
        }
    }
}
