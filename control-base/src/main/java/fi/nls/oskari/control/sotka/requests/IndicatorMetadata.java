package fi.nls.oskari.control.sotka.requests;

/**
 * Request class for SotkaNET statistics query to list indicator metadata.
 * @author SMAKINEN
 */
public class IndicatorMetadata extends SotkaRequest {

    public boolean isValid () {
        return getIndicator() != null && getIndicator().isEmpty();
    }
    @Override
    public String getName() {
        return "indicator_metadata";
    }

    @Override
    public String getRequestSpecificParams() {
        return "/indicators/" + getIndicator();
    }
}
