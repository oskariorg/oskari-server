package fi.nls.oskari.map.userlayer.domain;


import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.userlayer.service.GeoJsonWorker;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.File;

public class SHPGeoJsonCollection extends GeoJsonCollection implements GeoJsonWorker {


    final FeatureJSON io = new FeatureJSON();
    private static final Logger log = LogFactory
            .getLogger(SHPGeoJsonCollection.class);

    /**
     * Parse ESRI shape file set to geojson features
     * Coordinate transformation is executed, if shape .prj file is within
     * @param file   .shp import file
     * @param source_epsg   source CRS (is used, if crs in not available in source data)
     * @param target_epsg   target CRS
     * @return null --> ok   error message --> import failed
     */
    public String parseGeoJSON(File file, String source_epsg, String target_epsg) {
        ShapefileDataStore dataStore = null;
        try {

            dataStore = new ShapefileDataStore(file.toURI().toURL());
            String typeName = dataStore.getTypeNames()[0];
            MathTransform transform = null;
            FeatureSource source = dataStore.getFeatureSource(typeName);
            FeatureCollection collection = source.getFeatures();

            FeatureType schema = collection.getSchema();

            //Coordinate transformation support
            CoordinateReferenceSystem sourceCrs = source.getBounds().getCoordinateReferenceSystem();
            if (sourceCrs == null) sourceCrs = schema.getCoordinateReferenceSystem();
            //TODO check axis orientation

            //Geojson axis orientation is always  lon,lat (decode(....true)
            CoordinateReferenceSystem target = CRS.decode(target_epsg, true);

            if (sourceCrs == null && source_epsg== null ) {
                // Unknown CRS in source data - better to stop - result could be chaos
                return "Uknown projection data in the source import file " + file.getName();
            }

            // Source epsg not found in source data, use epsg given by the user
            if (sourceCrs == null) {
                sourceCrs = CRS.decode(source_epsg, true);
            }

            // Source and target are identical no transform ?  --> no transform
            if (sourceCrs != null && !target.getName().equals(sourceCrs.getName())){
                transform = CRS.findMathTransform(sourceCrs, target, true);
            }

            JSONArray feas = new JSONArray();
            FeatureIterator iterator = collection.features();
            while (iterator.hasNext()) {

                SimpleFeature feature = (SimpleFeature) iterator.next();
                if (transform != null) {
                    Geometry geometry = (Geometry) feature.getDefaultGeometry();
                    feature.setDefaultGeometry(JTS.transform(geometry, transform));

                }
                JSONObject geojs = JSONHelper.createJSONObject(io.toString(feature));
                if (geojs != null) {
                    feas.put(geojs);
                }
            }
            iterator.close();


            setGeoJson(JSONHelper.createJSONObject("features", feas));
            setFeatureType(schema);
            setTypeName(typeName);
            return null;

        }catch (Exception e) {
            log.error("Couldn't create geoJSON from the shp file ", file.getName(),
                    e);
            return "Couldn't create geoJSON from the shp file " + file.getName();
        }
        finally {
            dataStore.dispose();
        }
    }
}
