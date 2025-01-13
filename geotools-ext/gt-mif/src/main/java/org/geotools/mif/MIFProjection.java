package org.geotools.mif;

import java.util.Collections;
import java.util.Map;

import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.operation.DefiningConversion;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransformFactory;

public enum MIFProjection {

    WGS84(1) {
        @Override
        public CoordinateReferenceSystem toCRS(MIFDatum datum, String[] params) throws FactoryException {
            return datum.toGeographicCRS();
        }
    },
    Transverse_Mercator(8, 24) {
        @Override
        public CoordinateReferenceSystem toCRS(MIFDatum datum, String[] params) throws FactoryException {
            GeographicCRS geoCRS = datum.toGeographicCRS();

            CartesianCS cartCS = DefaultCartesianCS.PROJECTED;

            double origin_longitude = Double.parseDouble(params[0]);
            double origin_latitude = Double.parseDouble(params[1]);
            double scale_factor = Double.parseDouble(params[2]);
            double false_easting = Double.parseDouble(params[3]);
            double false_northing = Double.parseDouble(params[4]);

            MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
            ParameterValueGroup parameters = mtFactory.getDefaultParameters("Transverse_Mercator");
            parameters.parameter("central_meridian").setValue(origin_longitude);
            parameters.parameter("latitude_of_origin").setValue(origin_latitude);
            parameters.parameter("scale_factor").setValue(scale_factor);
            parameters.parameter("false_easting").setValue(false_easting);
            parameters.parameter("false_northing").setValue(false_northing);
            Conversion conversion = new DefiningConversion("Transverse_Mercator", parameters);

            Map<String, Object> properties = Collections.singletonMap("name", "unnamed");
            CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
            return crsFactory.createProjectedCRS(properties, geoCRS, conversion, cartCS);
        }
    },
    ;

    public final int[] type;

    private MIFProjection(int... type) {
        this.type = type;
    }

    public static MIFProjection find(int type) {
        for (MIFProjection it : values()) {
            for (int t : it.type) {
                if (t == type) {
                    return it;
                }
            }
        }
        return null;
    }

    public abstract CoordinateReferenceSystem toCRS(MIFDatum datum, String[] params) throws FactoryException;

}
