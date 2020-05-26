package org.geotools.mif;

import org.geotools.feature.simple.SimpleFeatureBuilder;

/**
 * The MID file is an optional file. When there is no MID file, all fields are blank.
 */
public class NoMIDReader extends MIDReader {

    @Override
    public void next(SimpleFeatureBuilder builder) {
        // NOP
    }

    @Override
    public void close() {
        // NOP
    }

}
