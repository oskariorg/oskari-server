package fi.nls.oskari.map.userlayer.domain;


import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.userlayer.service.GeoJsonWorker;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONObject;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.operation.MathTransform;

import java.io.File;

public class SHPGeoJsonCollection extends GeoJsonCollection implements GeoJsonWorker {


    final FeatureJSON io = new FeatureJSON();
    private static final Logger log = LogFactory
            .getLogger(SHPGeoJsonCollection.class);

    public boolean parseGeoJSON(File file) {

        try {

            ShapefileDataStore dataStore = new ShapefileDataStore(file.toURI().toURL());
            String typeName = dataStore.getTypeNames()[0];

            FeatureSource source = dataStore.getFeatureSource(typeName);
            FeatureCollection collection = source.getFeatures();

            FeatureType schema = collection.getSchema();
            //TODO: Coordinate transformation support
            // Helsinki City crs is 3879
            // CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:3879");
            // TODO add geotools specs to prj file   epsg:3879 data schema.getCoordinateReferenceSystem();
            // CoordinateReferenceSystem target = CRS.decode("EPSG:3067");

            MathTransform transform = null;
            //TODO: if (sourceCrs != null) transform = CRS.findMathTransform(sourceCrs, target);


            setGeoJson(JSONHelper.createJSONObject(io.toString(collection)));
            setFeatureType(schema);
            setTypeName(typeName);
            return true;

        } catch (Exception e) {
            log.error("Couldn't create geoJSON from the shp file",
                    e);
            return false;
        }
    }
}
