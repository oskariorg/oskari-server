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
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
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

public class GPXGeoJsonCollection extends GeoJsonCollection implements GeoJsonWorker {

    final FeatureJSON io = new FeatureJSON();
    private static final Logger log = LogFactory
            .getLogger(GPXGeoJsonCollection.class);

    /**
     * Parse MapInfo file set to geojson features
     * Coordinate transformation is executed, if shape .prj file is within
     * @param file .gpx import file
     * @param target_epsg target CRS
     * @return
     */
    public boolean parseGeoJSON(File file, String target_epsg) {
        OGRDataStoreFactory factory = new BridjOGRDataStoreFactory();
        Map<String, String> connectionParams = new HashMap<String, String>();
        connectionParams.put("DriverName", "GPX");
        connectionParams.put("DatasourceName", file.getAbsolutePath());
        DataStore store;
        String typeName = null;
        SimpleFeatureSource source;
        SimpleFeatureCollection collection;
        SimpleFeatureType schema = null;
        SimpleFeatureIterator it = null;
        CoordinateReferenceSystem sourceCrs = null;
        CoordinateReferenceSystem targetCrs = null;
        MathTransform transform = null;
        JSONArray features = null;

        try {
            store = factory.createDataStore(connectionParams);
            typeName = store.getTypeNames()[0];
            source = store.getFeatureSource(typeName);
            collection = source.getFeatures();
            it = collection.features();
            schema = collection.getSchema();

            // Transform
            // Gpx epsg:4326 and longitude 1st
            sourceCrs = CRS.decode("EPSG:4326",true);
            // Oskari crs
            //(oskari OL map crs)
            targetCrs = CRS.decode(target_epsg, true);
            if (!targetCrs.getName().equals(sourceCrs.getName())) {
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
             log.error("Couldn't create geoJSON from the GPX file",e);
             return false;
        }
    }
}
