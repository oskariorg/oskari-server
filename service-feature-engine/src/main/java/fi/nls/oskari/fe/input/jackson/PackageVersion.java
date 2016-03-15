package fi.nls.oskari.fe.input.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;


public  final class PackageVersion implements Versioned {
    public final static Version VERSION = new Version(2,4,3,"with geotools");

    @Override
    public Version version() {
        return VERSION;
    }
}
