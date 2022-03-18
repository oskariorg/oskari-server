package org.oskari.service.wfs.client;

import java.util.ArrayList;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerCapabilities;
import fi.nls.oskari.service.ServiceRuntimeException;

public class OskariWFSFilterFactory {
    private static FilterFactory ff = CommonFactoryFinder.getFilterFactory();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    protected static Filter getBBOXFilter (OskariLayer layer, ReferencedEnvelope bbox) {
        WFSLayerCapabilities caps = new WFSLayerCapabilities(layer.getCapabilities());
        String geomName = caps.getGeometryAttribute();
        return ff.bbox(geomName,
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY(),
                CRS.toSRS(bbox.getCoordinateReferenceSystem()));
    }
    protected static Filter getAttributeFilter(JSONObject filterJson) {
        return getAttributeFilter(filterJson.toString());
    }
    protected static Filter getAttributeFilter(String filterJson) {
        OskariWFSFilter filter = readJSONFilter(filterJson);
        return filter.getFilter();
    }

    private static OskariWFSFilter readJSONFilter(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, OskariWFSFilter.class);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Coudn't parse filter from: " + json, ex);
        }
    }
    protected static Filter appendFilter(final Filter main, final Filter toAppend) {
        if (main == null) {
            return toAppend;
        } else if (toAppend == null) {
            return main;
        } else {
            if (main instanceof And) {
                List<Filter> toAnd = new ArrayList<>(((And) main).getChildren());
                toAnd.add(toAppend);
                return ff.and(toAnd);
            } else if (toAppend instanceof And) {
                List<Filter> toAnd = new ArrayList<>(((And) toAppend).getChildren());
                toAnd.add(main);
                return ff.and(toAnd);
            } else {
                return smartAnd(main, toAppend);
            }
        }
    }

    // Avoid unncessary nesting of ANDs (x AND y) AND (z AND w)
    // => x AND y AND z AND w
    private static Filter smartAnd(final Filter a, final Filter b) {
        if (a instanceof And && b instanceof And) {
            List<Filter> _a = ((And) a).getChildren();
            List<Filter> _b = ((And) b).getChildren();
            List<Filter> and = new ArrayList<>(_a.size() + _b.size());
            and.addAll(_a);
            and.addAll(_b);
            return ff.and(and);
        } else if (a instanceof And) {
            List<Filter> _a = ((And) a).getChildren();
            List<Filter> and = new ArrayList<>(_a.size() + 1);
            and.addAll(_a);
            and.add(b);
            return ff.and(and);
        } else if (b instanceof And) {
            List<Filter> _b = ((And) b).getChildren();
            List<Filter> and = new ArrayList<>(_b.size() + 1);
            and.add(a);
            and.addAll(_b);
            return ff.and(and);
        } else {
            return ff.and(a, b);
        }
    }
}
