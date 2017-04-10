package fi.nls.oskari.control.statistics.xml;

import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.GML;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONObject;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Geometry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * We just need the name and id from here.
 * Example:
 * <p/>
 * <wfs:FeatureCollection numberMatched="320" numberReturned="320" timeStamp="2015-11-05T15:58:30.588Z"
 * xsi:schemaLocation="http://www.opengis.net/gml/3.2 http://localhost:8080/geoserver/schemas/gml/3.2.1/gml.xsd
 * http://www.opengis.net/wfs/2.0 http://localhost:8080/geoserver/schemas/wfs/2.0/wfs.xsd http://www.oskari.org
 * http://localhost:8080/geoserver/wfs?service=WFS&version=2.0.0&request=DescribeFeatureType&typeName=oskari%3Akunnat2013">
 * <wfs:boundedBy><gml:Envelope><gml:lowerCorner>83741.81960000005 6636325.0362</gml:lowerCorner><gml:upperCorner>732907.822 7776430.911</gml:upperCorner></gml:Envelope></wfs:boundedBy>
 * <wfs:member>
 * <oskari:kunnat2013 gml:id="kunnat2013.fid-2f003dff_150d823048f_-60b9">
 * <gml:boundedBy><gml:Envelope srsDimension="2" srsName="urn:ogc:def:crs:EPSG::3067"><gml:lowerCorner>321987.072 6959704.551</gml:lowerCorner><gml:upperCorner>366787.924 7005219.8</gml:upperCorner></gml:Envelope></gml:boundedBy>
 * <oskari:kuntanimi>Alajärvi</oskari:kuntanimi>
 * <oskari:kuntakoodi>005</oskari:kuntakoodi>
 * </oskari:kunnat2013>
 * </wfs:member> ...
 */
public class WfsXmlParser {

    private static final Logger LOG = LogFactory.getLogger(WfsXmlParser.class);

    public static List<Region> parse(InputStream inputStream, String idProperty, String nameProperty) throws ServiceException {

        List<Region> nameCodes = new ArrayList<>();
        SimpleFeatureCollection fc = getFeatureCollection(inputStream);
        SimpleFeatureIterator it = fc.features();

        while (it.hasNext()) {
            final SimpleFeature feature = it.next();
            Property id = feature.getProperty(idProperty);
            Property name = feature.getProperty(nameProperty);
            if (id == null || name == null) {
                throw new ServiceException("Couldn't find id (" + idProperty + ") and/or name(" + nameProperty +
                        ") property for region. Properties are:" + LOG.getAsString(feature.getProperties()));
            }
            Region region = new Region(
                    (String) id.getValue(),
                    (String) name.getValue());
            try {
                region.setPointOnSurface(getPointOnSurface(feature));
                region.setGeojson(getGeoJSON(feature, idProperty, nameProperty));
            } catch (Exception ex) {
                throw new ServiceRuntimeException("Error parsing region to GeoJson:" + region.toString(), ex);
            }
            nameCodes.add(region);
        }

        if (nameCodes.isEmpty()) {
            throw new ServiceException("Empty result, check configuration for region id-property=" +
                    idProperty + " and name-property =" + nameProperty);
        }
        return nameCodes;
    }

    private static SimpleFeatureCollection getFeatureCollection(InputStream inputStream) {
        try {
            GML gml = new GML(GML.Version.GML3);
            return gml.decodeFeatureCollection(inputStream);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Couldn't parse response to feature collection");
        }
    }

    private static JSONObject getGeoJSON(SimpleFeature feature, String idProp, String nameProp) throws IOException {
        FeatureJSON fj = new FeatureJSON();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        fj.writeFeature(feature, os);
        String json = os.toString();
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
