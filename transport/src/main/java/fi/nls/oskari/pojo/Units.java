package fi.nls.oskari.pojo;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.measure.unit.Unit;
import java.util.HashMap;

public class Units {
    private static final Logger log = LogFactory.getLogger(Units.class);
    private HashMap<String, Double> inchesPerUnits = new HashMap<String, Double>();

    public Units() {
        inchesPerUnits.put("m", 39.3701d);          // Meters
        inchesPerUnits.put("Meter", 39.3701d);      // Meters
        inchesPerUnits.put("dd", 4374754.0d);       // Degrees
        inchesPerUnits.put("degrees", 4374754.0d);  // Degrees
    }

    public double getScaleInSrs(double scale, String fromSrs, String toSrs) {
        CoordinateReferenceSystem fromCrs;
        CoordinateReferenceSystem toCrs;
        Unit<?> fromCrsUnit;
        Unit<?> toCrsUnit;
        String fromCrsUnitString;
        String toCrsUnitString;
        double normalizedScale = ((scale > 1.0) ? (1.0 / scale) : scale);
        double returnScale;

        log.debug("UNITS.JAVA: Scale", scale);
        log.debug("UNITS.JAVA: Normalized scale", normalizedScale);

        try {
            fromCrs = CRS.decode(fromSrs);
            toCrs = CRS.decode(toSrs);
            fromCrsUnit = fromCrs.getCoordinateSystem().getAxis(0).getUnit();
            toCrsUnit = toCrs.getCoordinateSystem().getAxis(0).getUnit();
            fromCrsUnitString = fromCrsUnit.toString();
            toCrsUnitString = toCrsUnit.toString();
            log.debug("UNITS.JAVA: from crs", fromCrsUnitString, "to crs", toCrsUnitString);

            if (inchesPerUnits.get(toCrsUnitString) == null || inchesPerUnits.get(fromCrsUnitString) == null) {
                log.warn("Did not know units", toCrsUnitString, fromCrsUnitString);
                return scale;
            }

            double toUnitFactor = inchesPerUnits.get(toCrsUnitString);
            double fromUnitFactor = inchesPerUnits.get(fromCrsUnitString);
            returnScale = toUnitFactor / (normalizedScale * fromUnitFactor);
            log.debug("UNITS.JAVA: to unit factor", toUnitFactor);
            log.debug("UNITS.JAVA: from unit factor", fromUnitFactor);
            log.debug("UNITS.JAVA: return scale", returnScale);
            return returnScale;
        } catch (Exception e) {
            log.error(e, "CRS decoding failed");
            return scale;
        }
    }
}
