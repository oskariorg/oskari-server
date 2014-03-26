package fi.nls.oskari.map.userlayer.domain;


import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.userlayer.service.GeoJsonWorker;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;

import org.geotools.geometry.jts.JTS;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.xml.PullParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.geotools.kml.bindings.DocumentTypeBinding;
import org.opengis.feature.simple.SimpleFeatureType;

import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.MathTransform;


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

            // Transform
            // Google kml epsg:4326
            //CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:4326");
            Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
            CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
            CoordinateReferenceSystem sourceCrs = factory.createCoordinateReferenceSystem("EPSG:4326");
            // Oskari crs
            // TODO: get crs from request  (oskari OL map crs)
            CoordinateReferenceSystem target = CRS.decode("EPSG:3067");
            MathTransform transform = CRS.findMathTransform(sourceCrs, target, true);
            // Switch direction, if 1st coord is to the north
            // boolean switchxy =(sourceCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.NORTH ||
            //       sourceCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.UP ||
            //     sourceCrs.getCoordinateSystem().getAxis(0).getDirection().absolute() == AxisDirection.DISPLAY_UP) ;


            ByteArrayOutputStream baos = new ByteArrayOutputStream();

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

            return true;

        } catch (Exception e) {
            log.error("Couldn't create geoJSON from the kml file",
                    e);
            return false;
        }
    }
}
