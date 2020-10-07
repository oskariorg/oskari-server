package org.oskari.service.wfs.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerCapabilities;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

public class OskariWFSFilter {
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
        WFSFilter wfsFilter = readJSONFilter(filterJson);
        return wfsFilter.getFilter();
    }

    public static WFSFilter readJSONFilter(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, WFSFilter.class);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Coudn't parse filter from: " + json, ex);
        }
    }
    protected static Filter appendFilter(final Filter main, final Filter toAppend) {
        if (main == null) {
            return toAppend;
        } else {
            return ff.and(main, toAppend);
        }
    }


}
