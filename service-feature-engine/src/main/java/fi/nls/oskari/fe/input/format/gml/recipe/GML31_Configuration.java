package fi.nls.oskari.fe.input.format.gml.recipe;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import org.geotools.gml3.GML;
import org.geotools.gml3.bindings.X_CurveTypeBinding;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;

public class GML31_Configuration extends org.geotools.gml3.GMLConfiguration {

    public GML31_Configuration() {
        super(false);
    }

    /**
     * Configures the gml3 context.
     * <p>
     * The following factories are registered:
     * <ul>
     * <li>{@link CoordinateArraySequenceFactory} under
     * {@link CoordinateSequenceFactory}
     * <li>{@link GeometryFactory}
     * </ul>
     * </p>
     */
    public void configureContext(MutablePicoContainer container) {
        super.setExtendedArcSurfaceSupport(true);
        super.configureContext(container);

    }

    /**
     * @see Configuration#configureBindings(java.util.Map)
     */
    @Override
    protected void registerBindings(MutablePicoContainer container) {
        super.setExtendedArcSurfaceSupport(true);
        super.registerBindings(container);

      // Tailored implementation for Curve segments parse  by Oskari
        container.registerComponentImplementation(GML.CurveType,
                X_CurveTypeBinding.class);

    }
}
