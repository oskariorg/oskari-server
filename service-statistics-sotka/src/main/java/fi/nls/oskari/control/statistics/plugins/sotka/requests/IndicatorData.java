package fi.nls.oskari.control.statistics.plugins.sotka.requests;

import java.io.StringWriter;

/**
 * Request class for SotkaNET statistics query to get indicator data in CSV format.
 * SotkaRequest transforms CSV to JSON since we defined isCSV() => true
 * @author SMAKINEN
 */
public class IndicatorData extends SotkaRequest {
    public final static String NAME = "data";

    public boolean isValid () {
        return getIndicator() != null && !getIndicator().isEmpty();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isCSV() {
        return true;
    }

    @Override
    public String getRequestSpecificParams() {
        StringWriter writer = new StringWriter();
        writer.write("/csv?indicator=");
        writer.write(getIndicator());
        for (String year : getYears()) {
            if (!year.isEmpty()) {
                writer.write("&years=");
                writer.write(year);
            }
        }
        if (!getGender().isEmpty()) {
            writer.write("&genders=");
            writer.write(getGender());
        }
        return writer.toString();
    }
}
