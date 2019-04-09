package fi.nls.oskari.search.channel;

import com.vividsolutions.jts.geom.Envelope;
import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeometryBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.geometry.primitive.Primitive;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.oskari.csw.request.GetRecords;
import org.oskari.geojson.GeoJSONReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class for creating search queries for MetadataCatalogue
 */
public class MetadataCatalogueQueryHelper {

    public final static String TARGET_SRS = "EPSG:4326";
    public final static String SPATIAL_OPERATOR = "INTERSECTS";

    private final static String WILDCARD_CHARACTER = "*";
    private final static String SINGLE_WILDCARD_CHARACTER = "?";
    private final static String ESCAPE_CHARACTER = "/";

    private static final Logger log = LogFactory.getLogger(MetadataCatalogueQueryHelper.class);
    private FilterFactory filterFactory;


    public MetadataCatalogueQueryHelper() {
        filterFactory = CommonFactoryFinder.getFilterFactory();
    }

    public String getQueryPayload(SearchCriteria searchCriteria) {
        final List<Filter> filters = getRecordsQuery(searchCriteria);
        if (filters.isEmpty()) {
            // no point in making the query without GetRecords, but throw exception instead?
            //throw new ServiceRuntimeException("Can't create GetRecords request without filters");
            return null;
        }
        Filter filter;
        if (filters.size() == 1) {
            filter = filters.get(0);
        } else {
            filter = filterFactory.and(filters);
        }

        return GetRecords.createRequest(filter);
    }

    public List<Filter> getRecordsQuery(SearchCriteria searchCriteria) {
        final List<Filter> list = new ArrayList<>();
        final List<Filter> theOrList = new ArrayList<>();

        // user input
        list.add(createLikeFilter(searchCriteria.getSearchString(), "csw:anyText"));

        for (MetadataField field : MetadataCatalogueChannelSearchService.getFields()) {
            final Filter operation = getFilterForField(searchCriteria, field);
            if (operation == null) {
                continue;
            }

            // add must matches to toplevel list
            if (field.isMustMatch()) {
                list.add(operation);
            }
            // others to OR-list
            else {
                theOrList.add(operation);
            }
        }
        if (theOrList.size() == 1) {
            list.add(theOrList.get(0));
        } else if (theOrList.size() > 1) {
            list.add(filterFactory.or(theOrList));
        }
        return list;
    }

    private Filter getFilterForField(SearchCriteria searchCriteria, MetadataField field) {
        return getFilterForField(searchCriteria, field, false);
    }

    private Filter getFilterForField(SearchCriteria searchCriteria, MetadataField field, boolean recursion) {
        final String[] values = getValuesForField(searchCriteria, field);
        if (values == null || (!recursion && field.getShownIf() != null)) {
            // FIXME: not too proud of the shownIf handling
            // shownIf is meant to link fields for frontend but it also means we need special handling for it in here
            // another field should have this one linked as dependency so we skip the actual field handling by default
            return null;
        }
        final Map<String, String> deps = field.getDependencies();
        log.debug("Field dependencies:", deps);
        final List<Filter> multiOp = new ArrayList<>();
        for (String value : values) {
            Filter op = getFilter(field, value);
            final String dep = deps.get(value);
            if (dep != null) {
                final MetadataField depField = MetadataCatalogueChannelSearchService.getField(dep);
                Filter depOp = getFilterForField(searchCriteria, depField, true);
                if (depOp != null) {
                    op = filterFactory.and(op, depOp);
                }
            }
            multiOp.add(op);
        }
        if (multiOp.isEmpty()) {
            return null;
        }

        if (field.isMulti() && multiOp.size() > 1) {
            // combine to one OR-statement if we have a multivalue field with more than one selection
            return filterFactory.or(multiOp);
        }
        return multiOp.get(0);
    }


    private String[] getValuesForField(SearchCriteria searchCriteria, MetadataField field) {
        if (searchCriteria == null || field == null) {
            return null;
        }

        Object param = searchCriteria.getParam(field.getProperty());
        if (param == null) {
            param = field.getDefaultValue();
        }
        if (param == null) {
            return null;
        }
        log.debug("Got value for metadata field:", field.getProperty(), "=", param);
        if (field.isMulti()) {
            return (String[]) param;
        } else {
            return new String[]{(String) param};
        }
    }

    private Filter getFilter(MetadataField field, String value) {
        if (field.getFilterOp() == null) {
            return createLikeFilter(value, field.getFilter());
        } else if (field.getFilterOp().equals(SPATIAL_OPERATOR)) {
            return getSpatialOperation(value, field.getFilter(), field.getFilterOp());
        } else {
            return createEqualsFilter(value, field.getFilter());
        }
    }


    private Filter createLikeFilter(final String searchCriterion,
                                    final String searchElementName) {
        if (searchCriterion == null || searchCriterion.isEmpty()) {
            return null;
        }
        Expression _property = filterFactory.property(searchElementName);
        return filterFactory.like(_property, searchCriterion, WILDCARD_CHARACTER, SINGLE_WILDCARD_CHARACTER, ESCAPE_CHARACTER, false);
    }

    private Filter createEqualsFilter(final String searchCriterion,
                                      final String searchElementName) {
        if (searchCriterion == null || searchCriterion.isEmpty()) {
            return null;
        }
        Expression _property = filterFactory.property(searchElementName);
        return filterFactory.equals(_property, filterFactory.literal(searchCriterion));
    }

    private Filter getSpatialOperation(final String searchCriterion,
                                       final String searchElementName, final String operation) {
        if (searchCriterion == null || searchCriterion.isEmpty()) {
            return null;
        }
/*
Deegree impl was something like:
 <ogc:Intersects>
 <ogc:PropertyName>ows:BoundingBox</ogc:PropertyName>
 <gml:Envelope>
 <gml:lowerCorner>14.05 46.46</gml:lowerCorner>
 <gml:upperCorner>17.24 48.42</gml:upperCorner>
 </gml:Envelope>
 </ogc:Intersects>
 */
        return createGeometryFilter(searchCriterion);
    }

    private String getSRS(JSONObject geojson) {
        JSONObject crs = geojson.optJSONObject("crs");
        if (crs != null) {
            // old ol2 impl: { ..., "crs":{"type":"name","properties":{"name":"EPSG:3067"}}}
            return crs.optJSONObject("properties").optString("name");
        }
        // new ol3+ drawtools impl: { ..., "crs":"EPSG:3067"}
        String srs = geojson.optString("crs");
        if (srs != null) {
            return srs;
        }
        return "EPSG:4326";
    }

    /* "{"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[382186.81433571,6677985.8855768],[382186.81433571,6682065.8855768],[391446.81433571,6682065.8855768],[391446.81433571,6677985.8855768],[382186.81433571,6677985.8855768]]]}}],"crs":{"type":"name","properties":{"name":"EPSG:3067"}}}" */
    private Filter createGeometryFilter(final String searchCriterion) {
        try {
            JSONObject geojson = JSONHelper.createJSONObject(searchCriterion);
            String sourceSRS = getSRS(geojson);
            JSONArray features = geojson.optJSONArray("features");
            if (features == null || features.length() != 1) {
                return null;
            }
            Envelope geom = GeoJSONReader.toGeometry(features.optJSONObject(0).optJSONObject("geometry")).getEnvelopeInternal();
            CoordinateReferenceSystem sourceCRS = CRS.decode(sourceSRS);
            CoordinateReferenceSystem targetCRS = CRS.decode(TARGET_SRS);

            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
            Envelope transformed = JTS.transform(geom, transform);


            GeometryBuilder gb = new GeometryBuilder(targetCRS);
            DirectPosition2D min = new DirectPosition2D(transformed.getMinX(), transformed.getMinY());
            min.setCoordinateReferenceSystem(targetCRS);
            DirectPosition2D max = new DirectPosition2D(transformed.getMaxX(), transformed.getMaxY());
            max.setCoordinateReferenceSystem(targetCRS);
            Primitive box = gb.createPrimitive(gb.createEnvelope(min, max));

            Filter bboxFilter = filterFactory.intersects("ows:BoundingBox", box);
            return bboxFilter;
/*
// TODO: remove stuff from previous implementation when this works:

            geojson.remove("crs");
            FeatureCollection fc = null;
            FeatureJSON fjs = new FeatureJSON();
            fc = fjs.readFeatureCollection(new ByteArrayInputStream(
                    searchCriterion.getBytes("utf-8")));
            ReferencedEnvelope bbox = fc.getBounds();
            try {
                bbox = bbox.transform(CRS.decode(TARGET_SRS), true);
            } catch (Exception e) {
                throw new ServiceRuntimeException("", e);
            }

            Point minb = ProjectionHelper.transformPoint(geom.getMinX(), geom.getMinY(), sourceSRS, TARGET_SRS);
            Point maxb = ProjectionHelper.transformPoint(geom.getMaxX(), geom.getMaxY(), sourceSRS, TARGET_SRS);

            //Filter bboxFilter = filterFactory.intersects("geom", fc.features().next().get);
            Filter bboxFilter = filterFactory.bbox("geom",
                    bbox.getMinX(), bbox.getMinY(),
                    bbox.getMaxX(), bbox.getMaxY(),
                    TARGET_SRS);
            return bboxFilter;
            */
/*
            StringBuilder sb = new StringBuilder("POLYGON((");
            //Transform to target crs

            sb.append(minb.getLonToString()+" ");
            sb.append(minb.getLatToString());
            sb.append(",");
            sb.append(minb.getLonToString()+" ");
            sb.append(maxb.getLatToString());
            sb.append(",");
            sb.append(maxb.getLonToString()+" ");
            sb.append(maxb.getLatToString());
            sb.append(",");
            sb.append(maxb.getLonToString()+" ");
            sb.append(minb.getLatToString());
            sb.append(",");
            sb.append(minb.getLonToString()+" ");
            sb.append(minb.getLatToString());
            sb.append("))");
            return sb.toString();
*/
        } catch (Exception e) {
            log.error(e, "Error parsing coverage geometry");
        }

        return null;
    }
}