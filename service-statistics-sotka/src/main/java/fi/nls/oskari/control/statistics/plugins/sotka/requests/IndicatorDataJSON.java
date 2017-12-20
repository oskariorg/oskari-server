package fi.nls.oskari.control.statistics.plugins.sotka.requests;

import java.io.StringWriter;

/**
 * Request class for SotkaNET statistics query to get indicator data in JSON format.
 * Data is: [{"indicator" : 4,"region": 231,"year": 2012,"gender": "total","value": 3.4,"absValue": 9}, ... ]
 * @author SMAKINEN
 */
public class IndicatorDataJSON extends SotkaRequest {
    public final static String NAME = "jsondata";

    public boolean isValid () {
        return getIndicator() != null && !getIndicator().isEmpty();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getRequestSpecificParams() {
        StringWriter writer = new StringWriter();
        writer.write("/json?indicator=");
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
