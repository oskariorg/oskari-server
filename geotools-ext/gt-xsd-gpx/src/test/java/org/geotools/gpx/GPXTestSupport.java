package org.geotools.gpx;

import org.geotools.xsd.Configuration;
import org.geotools.xsd.test.XMLTestSupport;

/**
 * Base test class for the http://www.topografix.com/GPX/1/1 schema.
 *
 * @generated
 */
public class GPXTestSupport extends XMLTestSupport {

    protected Configuration createConfiguration() {
        return new GPXConfiguration();
    }

}
