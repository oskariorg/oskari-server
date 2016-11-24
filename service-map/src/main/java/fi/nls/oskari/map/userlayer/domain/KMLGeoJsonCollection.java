package fi.nls.oskari.map.userlayer.domain;


import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.userlayer.service.GeoJsonWorker;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xml.PullParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.File;
import java.io.FileInputStream;

public class KMLGeoJsonCollection extends GeoJsonCollection implements GeoJsonWorker {


    final FeatureJSON io = new FeatureJSON();
    private static final Logger log = LogFactory
            .getLogger(KMLGeoJsonCollection.class);

    /**
     *  Parse Google kml import data to geojson features
     * @param file            kml import file
     * @param source_epsg source CRS (not in use in this format)
     * @param target_epsg     target CRS
     * @return null --> ok   error message --> import failed
     */
    public String parseGeoJSON(File file, String source_epsg, String target_epsg) {


        try {

            FileInputStream reader = new FileInputStream(file);
            PullParser parser = new PullParser(new KMLConfiguration(), reader, SimpleFeature.class);
            SimpleFeatureType featype = null;

            // Transform
            // Google kml epsg:4326  and longitude 1st
            CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:4326",true);

            // Oskari crs
            //(oskari OL map crs)
            CoordinateReferenceSystem target = CRS.decode(target_epsg, true);

            MathTransform transform = CRS.findMathTransform(sourceCrs, target, true);

            JSONArray feas = new JSONArray();
            SimpleFeature simpleFeature = (SimpleFeature) parser.parse();

            while (simpleFeature != null) {
                if (featype == null) {
                    featype = simpleFeature.getFeatureType();
                }
                // Transform
                Geometry geometry = (Geometry) simpleFeature.getDefaultGeometry();
                if (geometry != null) {
                    Geometry g2 = JTS.transform(geometry, transform);
                    simpleFeature.setDefaultGeometry(g2);

                    JSONObject geojs = JSONHelper.createJSONObject(io.toString(simpleFeature));
                    if (geojs != null) {
                        feas.put(geojs);
                    }
                }

                simpleFeature = (SimpleFeature) parser.parse();
            }

            setGeoJson(JSONHelper.createJSONObject("features", feas));
            // There is no schema in KML

            setFeatureType(featype);
            setTypeName("KML_");

            return null;

        } catch (Exception e) {
            log.error("Couldn't create geoJSON from the kml file ", file.getName(),
                    e);
            return "Couldn't create geoJSON from the kml file " + file.getName();
        }
    }
}
