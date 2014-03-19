package fi.nls.oskari.map.userlayer.domain;


import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.userlayer.service.GeoJsonWorker;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;

import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.xml.PullParser;
import org.json.JSONArray;
import org.opengis.feature.simple.SimpleFeature;
import org.geotools.kml.bindings.DocumentTypeBinding;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Geometry;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class KMLGeoJsonCollection extends GeoJsonCollection implements GeoJsonWorker {


    final FeatureJSON io = new FeatureJSON();
    private static final Logger log = LogFactory
            .getLogger(KMLGeoJsonCollection.class);

    public boolean parseGeoJSON(File file) {


        try {

            FileInputStream reader = new FileInputStream(file);
            PullParser parser = new PullParser(new KMLConfiguration(), reader, SimpleFeature.class);
            SimpleFeatureType featype = null;


            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            JSONArray feas = new JSONArray();
            SimpleFeature simpleFeature = (SimpleFeature) parser.parse();

            while (simpleFeature != null) {
                if (featype == null) {
                    featype = simpleFeature.getFeatureType();
                }
                feas.put(JSONHelper.createJSONObject(io.toString(simpleFeature)));

                simpleFeature = (SimpleFeature) parser.parse();
            }

            setGeoJson(JSONHelper.createJSONObject("features", feas));
            // There is no schema in KML

            setFeatureType(featype);
            setTypeName("KML_");

            return true;

        } catch (Exception e) {
            log.error("Couldn't create geoJSON from the shp file",
                    e);
            return false;
        }
    }
}
