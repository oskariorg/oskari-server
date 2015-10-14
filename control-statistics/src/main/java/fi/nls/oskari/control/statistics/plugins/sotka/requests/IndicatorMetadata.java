package fi.nls.oskari.control.statistics.plugins.sotka.requests;

/**
 * Request class for SotkaNET statistics query to list indicator metadata.
 * @author SMAKINEN
 */
public class IndicatorMetadata extends SotkaRequest {
    public final static String NAME = "indicator_metadata";

    public boolean isValid () {
        return getIndicator() != null && getIndicator().isEmpty();
    }
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getRequestSpecificParams() {
        return "/indicators/" + getIndicator();
    }
}
