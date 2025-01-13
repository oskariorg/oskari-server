package org.geotools.gpx.gpx10;

import org.geotools.xsd.Configuration;
import org.geotools.xsd.test.XMLTestSupport;

/**
 * Base test class for the http://www.topografix.com/GPX/1/0 schema.
 *
 * @generated
 */
public class GPX10TestSupport extends XMLTestSupport {

    protected Configuration createConfiguration() {
        return new GPX10Configuration();
    }

}
