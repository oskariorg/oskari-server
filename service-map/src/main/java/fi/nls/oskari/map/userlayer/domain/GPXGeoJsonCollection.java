package fi.nls.oskari.map.userlayer.domain;

import com.vividsolutions.jts.geom.Geometry;
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
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GPXGeoJsonCollection extends GeoJsonCollection implements GeoJsonWorker {
    static final int GJSON_DECIMALS = 10;
    GeometryJSON gjson = new GeometryJSON(GJSON_DECIMALS);
    final FeatureJSON io = new FeatureJSON(gjson);
    private static final Logger log = LogFactory
            .getLogger(GPXGeoJsonCollection.class);

    /**
     * Parse MapInfo file set to geojson features
     * Coordinate transformation is executed, if shape .prj file is within
     * @param file .gpx import file
     * @param source_epsg source CRS (not in use in this format)
     * @param target_epsg target CRS
     * @return null --> ok   error message --> import failed
     */
    public String parseGeoJSON(File file, String source_epsg, String target_epsg) {
        OGRDataStoreFactory factory = new BridjOGRDataStoreFactory();
        if(!factory.isAvailable()){
            log.error("GDAL library is not found for GPX import -- http://www.gdal.org/");
            return "gpx";
        }
        Map<String, String> connectionParams = new HashMap<String, String>();
        connectionParams.put("DriverName", "GPX");
        connectionParams.put("DatasourceName", file.getAbsolutePath());
        DataStore store = null;
        SimpleFeatureSource source;
        SimpleFeatureCollection collection;
        SimpleFeatureIterator it;
        SimpleFeature feature;
        SimpleFeatureType featureType = null;
        CoordinateReferenceSystem sourceCrs;
        CoordinateReferenceSystem targetCrs;
        MathTransform transform = null;
        JSONArray features = new JSONArray();

        try {
            // Transform
            // Gpx epsg:4326 and longitude 1st
            sourceCrs = CRS.decode("EPSG:4326",true);
            // Oskari crs
            //(oskari OL map crs)
            targetCrs = CRS.decode(target_epsg, true);
            if (!targetCrs.getName().equals(sourceCrs.getName())) {
                transform = CRS.findMathTransform(sourceCrs, targetCrs, true);
            }
            store = factory.createDataStore(connectionParams);
            String[] typeNames = store.getTypeNames();
            for (String typeName : typeNames) {
                // Skip track points
                if (typeName.equals("track_points")) {
                    continue;
                }
                source = store.getFeatureSource(typeName);
                collection = source.getFeatures();
                it = collection.features();
                while (it.hasNext()) {
                    feature = it.next();
                    featureType = feature.getFeatureType();
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
            }
            if (features.length() > 0) {
                setGeoJson(JSONHelper.createJSONObject("features", features));
                setFeatureType((FeatureType)featureType);
                setTypeName("GPX_");
            }
        } catch (Exception e) {
             log.error("Couldn't create geoJSON from the GPX file ", file.getName(), e);
             return "gpx";
        }
        finally {
            store.dispose();
        }
        return null;
    }
}
