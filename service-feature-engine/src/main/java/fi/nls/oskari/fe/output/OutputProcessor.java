package fi.nls.oskari.fe.output;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.schema.XSDDatatype;
import org.json.JSONObject;

public interface OutputProcessor {

    /*
     * Note ! links to multiple elements with geometry props in properties NOT
     * in geometryProperties
     */
    /* assuming GeometryCollection is a Geometry too */

    public void begin() throws IOException;

    public void edge(final Resource subject, final Resource predicate,
            final Resource value) throws IOException;

    public void end() throws IOException;

    public void flush() throws IOException;

    public void prefix(final String prefix, final String ns) throws IOException;

    public void type(final Resource type,
            final List<Pair<Resource, XSDDatatype>> simpleProperties,
            final List<Pair<Resource, Object>> linkProperties,
            final List<Pair<Resource, String>> geometryProperties)
            throws IOException;

    public void vertex(final Resource iri, final Resource type,
            final List<Pair<Resource, Object>> simpleProperties,
            final List<Pair<Resource, Object>> linkProperties)
            throws IOException;

    public void vertex(final Resource iri, final Resource type,
            final List<Pair<Resource, Object>> simpleProperties,
            final List<Pair<Resource, Object>> linkProperties,
            final List<Pair<Resource, Geometry>> geometryProperties)
            throws IOException;

    public void merge(final List<JSONObject> list, Resource href) throws  IOException;

}
