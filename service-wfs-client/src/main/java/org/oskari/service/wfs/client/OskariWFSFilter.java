package org.oskari.service.wfs.client;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerCapabilities;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import java.util.*;
import java.util.stream.Collectors;

public class OskariWFSFilter {
    private static final String KEY_PROPERTY = "property";
    private static final String KEY = "key";
    private static final String KEY_AND = "AND";
    private static final String KEY_OR = "OR";
    private static final String KEY_MATCH_CASE = "caseSensitive";
    private static final String KEY_EQUAL = "value";
    // Number and range
    private static final String KEY_GREATER = "greaterThan";
    private static final String KEY_GREATER_OR_EQUAL = "atLeast";
    private static final String KEY_LESS = "lessThan";
    private static final String KEY_LESS_OR_EQUAL = "atMost";

    private static final String KEY_LIKE = "like";
    private static final String KEY_NOT_LIKE = "notLike";
    private static final String KEY_IN = "in";
    private static final String KEY_NOT_IN = "notIn";

    private static FilterFactory ff = CommonFactoryFinder.getFilterFactory();

    protected static Filter getBBOXFilter (OskariLayer layer, ReferencedEnvelope bbox) {
        WFSLayerCapabilities caps = new WFSLayerCapabilities(layer.getCapabilities());
        String geomName = caps.getGeometryAttribute();
        return ff.bbox(geomName,
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY(),
                CRS.toSRS(bbox.getCoordinateReferenceSystem()));
    }

    protected static Filter getFilter(JSONObject filterJson) {
        Filter filter = null;
        JSONObject prop = JSONHelper.getJSONObject(filterJson, KEY_PROPERTY);
        if (prop != null) {
            filter = getPropertyFilter(prop);
        }
        List <Filter> ands = JSONHelper.getArrayAsList(JSONHelper.getJSONArray(filterJson, KEY_AND))
                .stream().map(f -> getPropertyFilter((JSONObject) f)).collect(Collectors.toList());
        if (!ands.isEmpty()) {
            filter = appendFilter(filter, ff.and(ands));
        }
        List <Filter> ors = JSONHelper.getArrayAsList(JSONHelper.getJSONArray(filterJson, KEY_OR))
                .stream().map(f -> getPropertyFilter((JSONObject) f)).collect(Collectors.toList());
        if (!ors.isEmpty()) {
            filter = appendFilter(filter, ff.or(ors));
        }

        return filter;
    }
    private static Filter appendFilter(final Filter main, final Filter toAppend) {
        if (main == null) {
            return toAppend;
        } else {
            return ff.and(main, toAppend);
        }
    }
    private static Filter getPropertyFilter(JSONObject property) {
        String key = JSONHelper.optString(property, KEY);
        boolean match = JSONHelper.getBooleanFromJSON(property,KEY_MATCH_CASE, true);
        Expression name = ff.property(key);
        if (property.has(KEY_EQUAL)) {
            return ff.equal(name, ff.literal(JSONHelper.get(property, KEY_EQUAL)), match);
        }
        if (property.has(KEY_GREATER)) {
            Filter greater = ff.greater(name, ff.literal(JSONHelper.get(property, KEY_GREATER)));
            if (property.has(KEY_LESS)) {
                Filter less = ff.less(name, ff.literal(JSONHelper.get(property, KEY_LESS)));
                return ff.and(greater, less);
            }
            if (property.has(KEY_LESS_OR_EQUAL)) {
                Filter lessEqual = ff.lessOrEqual(name, ff.literal(JSONHelper.get(property, KEY_LESS_OR_EQUAL)));
                return ff.and(greater, lessEqual);
            }
            return greater;
        }
        if (property.has(KEY_IN)) {
            List <Filter> values = JSONHelper.getArrayAsList(JSONHelper.getJSONArray(property,KEY_IN))
                    .stream()
                    .map(value -> ff.equal(name, ff.literal(value), match))
                    .collect(Collectors.toList());
            return ff.or(values);
        }
        if (property.has(KEY_NOT_IN)) {
            List <Filter> values = JSONHelper.getArrayAsList(JSONHelper.getJSONArray(property,KEY_NOT_IN))
                    .stream()
                    .map(value -> ff.notEqual(name, ff.literal(value), match))
                    // .map(filter -> property.has(KEY_NOT_IN) ? ff.not(filter):filter)
                    .collect(Collectors.toList());
            return ff.or(values);
        }
        if (property.has(KEY_LIKE)) {
            List <String> values = JSONHelper.getArrayAsList(JSONHelper.getJSONArray(property, KEY_LIKE));
            String value = JSONHelper.optString(property, KEY_LIKE, "");
            if (!value.isEmpty()) {
                values = Collections.singletonList(value);
            }
            List<Filter> filters = values.stream()
                    .map(val -> ff.like (name, val))
                    .collect(Collectors.toList());
            return ff.or(filters);
        }
        if (property.has(KEY_NOT_LIKE)) {
            List <String> values = JSONHelper.getArrayAsList(JSONHelper.getJSONArray(property, KEY_NOT_LIKE));
            String value = JSONHelper.optString(property, KEY_NOT_LIKE, "");
            if (!value.isEmpty()) {
                values = Collections.singletonList(value);
            }
            List<Filter> filters = values.stream()
                    .map(val -> ff.like (name, val))
                    .map(f -> ff.not(f))
                    .collect(Collectors.toList());
            return ff.or(filters);
        }
        throw new IllegalArgumentException("Invalid filter: " + property.toString() );
    }

}
