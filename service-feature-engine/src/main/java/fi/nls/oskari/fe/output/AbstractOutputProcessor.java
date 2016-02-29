package fi.nls.oskari.fe.output;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractOutputProcessor implements OutputProcessor {

    protected final Map<String, String> nsToPrefix = new HashMap<String, String>();

    public Map<Resource, Geometry> geometryMap() {
        return new LinkedHashMap<Resource, Geometry>();
    }

    public Map<Resource, Object> map() {
        return new LinkedHashMap<Resource, Object>();
    }

    public void prefix(String prefix, String ns) throws IOException {

        nsToPrefix.put(ns, prefix);

    }

    public String prefixedResource(Resource rc) {
        String ns = nsToPrefix.get(rc.getNs());
        return rc.toString(ns, ":");

    }

}
