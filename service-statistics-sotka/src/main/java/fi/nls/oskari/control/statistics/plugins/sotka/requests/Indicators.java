package fi.nls.oskari.control.statistics.plugins.sotka.requests;

/**
 * Request class for SotkaNET statistics query to list indicators.
 * @author SMAKINEN
 */
public class Indicators extends SotkaRequest {

    public final static String NAME = "indicators";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getRequestSpecificParams() {
        return "/indicators";
    }

}
