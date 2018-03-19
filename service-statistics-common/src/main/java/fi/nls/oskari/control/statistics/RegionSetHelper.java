package fi.nls.oskari.control.statistics;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.control.statistics.db.RegionSet;
import fi.nls.oskari.control.statistics.xml.Region;
import fi.nls.oskari.control.statistics.xml.WfsXmlParser;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;

public class RegionSetHelper {

    private static final Logger LOG = LogFactory.getLogger(RegionSetService.class);

    private static final String RESOURCES_URL_PREFIX = "resources://";
    private static final FeatureJSON FJ = new FeatureJSON();

    public static List<Region> getRegions(RegionSet regionset, String requestedSRS)
            throws FactoryException, MismatchedDimensionException, TransformException, ServiceException, IOException {
        SimpleFeatureCollection fc = getFeatureCollection(regionset, requestedSRS);
        final String propId = regionset.getIdProperty();
        final String propName = regionset.getNameProperty();
        return parse(fc, propId, propName);
    }

    protected static SimpleFeatureCollection getFeatureCollection(RegionSet regionset, String requestedSRS)
            throws FactoryException, MismatchedDimensionException, TransformException, ServiceException, IOException {
        String url = regionset.getFeaturesUrl();
        if (url.startsWith(RESOURCES_URL_PREFIX)) {
            return getRegionsResources(regionset, requestedSRS);
        } else {
            return getRegionsWFS(regionset, requestedSRS);
        }
    }

    protected static SimpleFeatureCollection getRegionsResources(RegionSet regionset, String requestedSRS)
            throws IOException, MismatchedDimensionException, TransformException, FactoryException {
        String url = regionset.getFeaturesUrl();
        String path = url.substring(RESOURCES_URL_PREFIX.length());
        if (path.toLowerCase().endsWith(".json")) {
            return getRegionsResourcesGeoJSON(regionset, requestedSRS, path);
        }
        throw new IllegalArgumentException("Invalid resource file format");
    }

    /**
     * Read (Simple)FeatureCollection from GeoJSON resource file
     * transforming geometries to the requestedSRS
     */
    protected static SimpleFeatureCollection getRegionsResourcesGeoJSON(RegionSet regionset, String requestedSRS, String path)
            throws IOException, MismatchedDimensionException, TransformException, FactoryException {
        MathTransform transform = findMathTransform(regionset.getSrs_name(), requestedSRS);
        FeatureIterator<SimpleFeature> it = null;
        try (InputStream in = RegionSetHelper.class.getResourceAsStream(path)) {
            DefaultFeatureCollection fc = new DefaultFeatureCollection();
            it = FJ.streamFeatureCollection(in);
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                transform(f, transform);
                fc.add(f);
            }
            return fc;
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    protected static MathTransform findMathTransform(String from, String to) throws FactoryException {
        if (from.equals(to)) {
            return null;
        }
        CoordinateReferenceSystem sourceCRS = CRS.decode(from);
        CoordinateReferenceSystem targetCRS = CRS.decode(to);
        return CRS.findMathTransform(sourceCRS, targetCRS, true);
    }

    protected static void transform(SimpleFeature f, MathTransform transform)
            throws MismatchedDimensionException, TransformException {
        if (transform != null) {
            Object geometry = f.getDefaultGeometry();
            if (geometry != null && geometry instanceof Geometry) {
                JTS.transform((Geometry) geometry, transform);
            }
        }
    }

    protected static SimpleFeatureCollection getRegionsWFS(RegionSet regionset, String requestedSRS)
            throws ServiceException, IOException {
        // For example: http://localhost:8080/geoserver/wfs?service=wfs&version=1.1.0&request=GetFeature&typeNames=oskari:kunnat2013
        Map<String, String> params = new HashMap<>();
        params.put("service", "wfs");
        params.put("version", "1.1.0");
        params.put("request", "GetFeature");
        params.put("typeName", regionset.getName());
        params.put("srsName", requestedSRS);
        final String url = IOHelper.constructUrl(regionset.getFeaturesUrl(), params);
        final HttpURLConnection connection = IOHelper.getConnection(url);
        try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
            return WfsXmlParser.getFeatureCollection(in);
        }
    }

    protected static List<Region> parse(SimpleFeatureCollection fc, String idProperty, String nameProperty)
            throws ServiceException {
        SimpleFeatureIterator it = fc.features();
        try {
            List<Region> nameCodes = new ArrayList<>();
            while (it.hasNext()) {
                final SimpleFeature feature = it.next();
                Property id = feature.getProperty(idProperty);
                Property name = feature.getProperty(nameProperty);
                if (id == null || name == null) {
                    LOG.warn("Couldn't find id (" + idProperty + ") and/or name(" + nameProperty +
                            ") property for region. Properties are:" + LOG.getAsString(feature.getProperties()));
                    continue;
                }
                Region region = new Region(
                        (String) id.getValue(),
                        (String) name.getValue());
                try {
                    region.setPointOnSurface(getPointOnSurface(feature));
                    region.setGeojson(toGeoJSON(feature, idProperty, nameProperty));
                    nameCodes.add(region);
                } catch (Exception ex) {
                    LOG.warn("Region had invalid geometry:", region, "Error:", ex.getMessage());
                }
            }

            if (nameCodes.isEmpty()) {
                throw new ServiceException("Empty result, check configuration for region id-property=" +
                        idProperty + " and name-property =" + nameProperty);
            }
            return nameCodes;
        } finally {
            it.close();
        }
    }

    protected static JSONObject toGeoJSON(SimpleFeature feature, String idProp, String nameProp) throws IOException {
        String json = FJ.toString(feature);
        JSONObject jsonObj = JSONHelper.createJSONObject(json);
        JSONObject props = getProps(jsonObj);
        if(props != null) {
            String id = props.optString(idProp);
            String name = props.optString(nameProp);
            JSONObject newProps = new JSONObject();
            JSONHelper.putValue(newProps, Region.KEY_CODE, id);
            JSONHelper.putValue(newProps, Region.KEY_NAME, name);
            JSONHelper.putValue(jsonObj, "properties", newProps);
        }
        return jsonObj;
    }

    private static JSONObject getProps(JSONObject geojson) {
        if(geojson == null) {
            return null;
        }
        return geojson.optJSONObject("properties");
    }

    private static Point getPointOnSurface(SimpleFeature feature) {

        Geometry geometry = (Geometry)feature.getDefaultGeometry();
        // " An interior point is guaranteed to lie in the interior of the Geometry, if it possible to
        // calculate such a point exactly. Otherwise, the point may lie on the boundary of the geometry."
        com.vividsolutions.jts.geom.Point pos = geometry.getInteriorPoint();
        return new Point(pos.getX(), pos.getY());
    }


}