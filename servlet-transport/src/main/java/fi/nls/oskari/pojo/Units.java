package fi.nls.oskari.pojo;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.measure.unit.Unit;
import java.util.HashMap;

public class Units {
    private static final Logger log = LogFactory.getLogger(Units.class);
    private HashMap<String, Double> metersPerUnits = new HashMap<String, Double>();

    public Units() {
        metersPerUnits.put("in", 0.0254d);                      // Inches
        metersPerUnits.put("inches", 0.0254d);                  // Inches
        metersPerUnits.put("Inch", 0.0254d);                    // Inches
        metersPerUnits.put("m", 1.0d);                          // Meters
        metersPerUnits.put("metre", 1.0d);                      // Meters
        metersPerUnits.put("Meter", 1.0d);                      // Meters
        metersPerUnits.put("°", 0.01745329251994328d);         // Degrees
        metersPerUnits.put("dd", 0.01745329251994328d);        // Degrees
        metersPerUnits.put("degree", 0.01745329251994328d);    // Degrees
        metersPerUnits.put("degrees", 0.01745329251994328d);   // Degrees
        metersPerUnits.put("degree_angle", 0.01745329251994328d); // Degrees
        metersPerUnits.put("ft", 0.3048d);                      // Feet
        metersPerUnits.put("foot", 0.3048d);                    // Feet
        metersPerUnits.put("Foot", 0.3048d);                    // Feet
        metersPerUnits.put("foot_survey_us", 0.3048d);          // Feet
        metersPerUnits.put("mi", 1609.344d);                    // Miles
        metersPerUnits.put("Mile", 1609.344d);                  // Miles
        metersPerUnits.put("km", 1000.0d);                      // Kilometers
        metersPerUnits.put("kilometer", 1000.0d);               // Kilometers
        metersPerUnits.put("kilometre", 1000.0d);               // Kilometers
        metersPerUnits.put("Kilometer", 1000.0d);               // Kilometers
        metersPerUnits.put("yd", 0.914399204289812d);           // Yards
        metersPerUnits.put("Yard", 0.914399204289812d);         // Yards
        metersPerUnits.put("yard", 0.914399204289812d);         // Yards
        metersPerUnits.put("nmi", 1852.0d);                     // Nautical miles
        metersPerUnits.put("kmi", 1852.0d);                     // Nautical miles
        metersPerUnits.put("NautM", 1852.0d);                   // Nautical miles
        metersPerUnits.put("fathom", 1.8288d);                  // International fathom
        metersPerUnits.put("Fathom", 1.8288d);                  // International fathom
        metersPerUnits.put("fath", 1.8288d);                    // International fathom
        metersPerUnits.put("gradians", 0.0157079633267949d);    // Gradians
        metersPerUnits.put("grad", 0.0157079633267949d);        // Gradians
        metersPerUnits.put("gr", 0.0157079633267949d);          // Gradians
    }

    /**
     * Returns the scale transformed from one SRS to another,
     * or the scale given as an argument if the transformation
     * fails or if one of the units is unknown.
     *
     * @param scale
     * @param fromSrs
     * @param toSrs
     */
    public double getScaleInSrs(double scale, String fromSrs, String toSrs) {
        CoordinateReferenceSystem fromCrs;
        CoordinateReferenceSystem toCrs;
        Unit<?> fromCrsUnit;
        Unit<?> toCrsUnit;
        String fromCrsUnitString;
        String toCrsUnitString;
        double toUnitFactor;
        double fromUnitFactor;

        try {
            fromCrs = CRS.decode(fromSrs);
            toCrs = CRS.decode(toSrs);
            fromCrsUnit = fromCrs.getCoordinateSystem().getAxis(0).getUnit();
            toCrsUnit = toCrs.getCoordinateSystem().getAxis(0).getUnit();
            fromCrsUnitString = fromCrsUnit.toString();
            toCrsUnitString = toCrsUnit.toString();

            if (metersPerUnits.get(fromCrsUnitString) == null) {
                log.warn("Unknown unit", fromCrsUnitString);
                return scale;
            }
            fromUnitFactor = metersPerUnits.get(fromCrsUnitString);

            if (metersPerUnits.get(toCrsUnitString) == null) {
                log.warn("Unknown unit", toCrsUnitString);
                return scale;
            }
            toUnitFactor = metersPerUnits.get(toCrsUnitString);

            log.debug("Units: from unit", fromCrsUnitString, ", to unit", toCrsUnitString);

            return scale * toUnitFactor / fromUnitFactor;
        } catch (Exception e) {
            log.error(e, "CRS decoding failed");
            return scale;
        }
    }
}
