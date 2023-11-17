package fi.nls.oskari.control.statistics.plugins.sotka.requests;

/**
 * Request class for SotkaNET statistics query to list regions.
 * @author SMAKINEN
 */
public class Regions extends SotkaRequest {

    public final static String NAME = "regions";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getRequestSpecificParams() {
        return "/regions";
    }

}
