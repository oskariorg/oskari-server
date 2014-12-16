package fi.nls.oskari.map.userlayer.domain;


import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.userlayer.service.GeoJsonWorker;
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
import com.vividsolutions.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class SHPGeoJsonCollection extends GeoJsonCollection implements GeoJsonWorker {


    final FeatureJSON io = new FeatureJSON();
    private static final Logger log = LogFactory
            .getLogger(SHPGeoJsonCollection.class);

    /**
     * Parse ESRI shape file set to geojson features
     * Coordinate transformation is executed, if shape .prj file is within
     * @param file   .shp import file
     * @param target_epsg   target CRS
     * @return
     */
    public boolean parseGeoJSON(File file, String target_epsg) {

        try {

            ShapefileDataStore dataStore = new ShapefileDataStore(file.toURI().toURL());
            String typeName = dataStore.getTypeNames()[0];

            FeatureSource source = dataStore.getFeatureSource(typeName);
            FeatureCollection collection = source.getFeatures();

            FeatureType schema = collection.getSchema();

            //Coordinate transformation support
            CoordinateReferenceSystem sourceCrs = source.getBounds().getCoordinateReferenceSystem();
            if(sourceCrs == null) sourceCrs = schema.getCoordinateReferenceSystem();

            // TODO get map crs from request (current OL map crs)
            CoordinateReferenceSystem target = CRS.decode(target_epsg);
            // Source and target are identical ?
            // TODO: better check algorithm - name is not 100% proof
            if(sourceCrs != null && target.getName().equals(sourceCrs.getName())) sourceCrs = null;

            MathTransform transform = null;
            JSONArray feas = new JSONArray();

            if (sourceCrs != null) transform = CRS.findMathTransform(sourceCrs, target, true);

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
            return true;

        } catch (Exception e) {
            log.error("Couldn't create geoJSON from the shp file ", file.getName(),
                    e);
            return false;
        }
    }
}
