package fi.nls.oskari.fe.input.format.gml.recipe;

import org.geotools.gml3.ArcParameters;
import org.geotools.gml3.GML;
import org.geotools.gml3.bindings.RingTypeBinding;
import org.geotools.gml3.bindings.SurfacePatchArrayPropertyTypeBinding;
import org.geotools.gml3.bindings.X_ArcStringTypeBinding;
import org.geotools.gml3.bindings.X_ArcTypeBinding;
import org.geotools.gml3.bindings.X_CircleTypeBinding;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

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
        super.configureContext(container);

        container.registerComponentInstance(new ArcParameters());

    }

    /**
     * @see Configuration#configureBindings(java.util.Map)
     */
    @Override
    protected void registerBindings(MutablePicoContainer container) {
        super.registerBindings(container);

        container.registerComponentImplementation(GML.ArcStringType,
                X_ArcStringTypeBinding.class);
        container.registerComponentImplementation(GML.ArcType,
                X_ArcTypeBinding.class);
        container.registerComponentImplementation(GML.CircleType,
                X_CircleTypeBinding.class);
        /*
         * container.registerComponentImplementation(GML.ArcStringType,
         * ArcStringTypeBinding.class);
         * container.registerComponentImplementation(GML.ArcType,
         * ArcTypeBinding.class);
         * container.registerComponentImplementation(GML.CircleType,
         * CircleTypeBinding.class);
         */
        container.registerComponentImplementation(GML.RingType,
                RingTypeBinding.class);
        container.registerComponentImplementation(
                GML.SurfacePatchArrayPropertyType,
                SurfacePatchArrayPropertyTypeBinding.class);
        container
                .registerComponentImplementation(
                        GML.CurveArrayPropertyType,
                        org.geotools.gml3.bindings.ext.CurveArrayPropertyTypeBinding.class);
        container.registerComponentImplementation(GML.CurvePropertyType,
                org.geotools.gml3.bindings.ext.CurvePropertyTypeBinding.class);
        container.registerComponentImplementation(GML.CurveType,
                org.geotools.gml3.bindings.ext.CurveTypeBinding.class);
        container.registerComponentImplementation(GML.MultiCurveType,
                org.geotools.gml3.bindings.ext.MultiCurveTypeBinding.class);
        container.registerComponentImplementation(GML.MultiPolygonType,
                org.geotools.gml3.bindings.ext.MultiPolygonTypeBinding.class);
        container.registerComponentImplementation(GML.MultiSurfaceType,
                org.geotools.gml3.bindings.ext.MultiSurfaceTypeBinding.class);
        container.registerComponentImplementation(GML.PolygonPatchType,
                org.geotools.gml3.bindings.ext.PolygonPatchTypeBinding.class);
        container
                .registerComponentImplementation(
                        GML.SurfaceArrayPropertyType,
                        org.geotools.gml3.bindings.ext.SurfaceArrayPropertyTypeBinding.class);
        container
                .registerComponentImplementation(
                        GML.SurfacePatchArrayPropertyType,
                        org.geotools.gml3.bindings.ext.SurfacePatchArrayPropertyTypeBinding.class);
        container
                .registerComponentImplementation(
                        GML.SurfacePropertyType,
                        org.geotools.gml3.bindings.ext.SurfacePropertyTypeBinding.class);
        container.registerComponentImplementation(GML.SurfaceType,
                org.geotools.gml3.bindings.ext.SurfaceTypeBinding.class);

    }
}
