package fi.nls.oskari.search.channel;

import com.vividsolutions.jts.geom.Geometry;
import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
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

    public final static String WILDCARD_CHARACTER = "*";
    public final static String SINGLE_WILDCARD_CHARACTER = "?";
    public final static String ESCAPE_CHARACTER = "/";

    private static final Logger log = LogFactory.getLogger(MetadataCatalogueQueryHelper.class);
    private FilterFactory2 filterFactory;


    public MetadataCatalogueQueryHelper() {
        filterFactory = CommonFactoryFinder.getFilterFactory2();
    }

    public String getQueryPayload(SearchCriteria searchCriteria) {
        final List<Filter> filters = getFiltersForQuery(searchCriteria);
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

    public List<Filter> getFiltersForQuery(SearchCriteria searchCriteria) {
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
            return getSpatialOperation(value);
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

    private Filter getSpatialOperation(final String searchCriterion) {
        if (searchCriterion == null || searchCriterion.isEmpty()) {
            return null;
        }
        return createGeometryFilter(searchCriterion);
    }

    private String getSRS(JSONObject geojson) {
        // The frontend gives us this
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
    public Filter createGeometryFilter(final String searchCriterion) {
            try {
                JSONObject geojson = JSONHelper.createJSONObject(searchCriterion);
                String sourceSRS = getSRS(geojson);
                JSONArray features = geojson.optJSONArray("features");
                if (features == null || features.length() != 1) {
                    return null;
                }
                Geometry geom = GeoJSONReader.toGeometry(features.optJSONObject(0).optJSONObject("geometry"));
                CoordinateReferenceSystem sourceCRS = CRS.decode(sourceSRS);
                CoordinateReferenceSystem targetCRS = CRS.decode(TARGET_SRS);

                MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
                Geometry transformed = JTS.transform(geom, transform);

                return filterFactory.intersects(
                        filterFactory.property("ows:BoundingBox"),
                        filterFactory.literal( transformed ));
            } catch (Exception e) {
                throw new ServiceRuntimeException("Can't create GetRecords request with coverage filter", e);
            }
    }
}