package org.geotools.gpx;

import org.geotools.xsd.Configuration;
import org.picocontainer.MutablePicoContainer;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;

/**
 * Parser configuration for the http://www.topografix.com/GPX/1/1 schema.
 *
 * @generated
 */
public class GPXConfiguration extends Configuration {

    /**
     * Creates a new configuration.
     *
     * @generated
     */
    public GPXConfiguration() {
        super(GPX.getInstance());

        //TODO: add dependencies here
    }

    protected void configureContext(MutablePicoContainer container) {
        container.registerComponentInstance(new GeometryFactory());
        container.registerComponentInstance(CoordinateArraySequenceFactory.instance());
    }

    /**
     * Registers the bindings for the configuration.
     *
     * @generated
     */
    protected final void registerBindings( MutablePicoContainer container ) {
        //Types

        // container.registerComponentImplementation(GPX.boundsType,BoundsTypeBinding.class);
        // container.registerComponentImplementation(GPX.copyrightType,CopyrightTypeBinding.class);
        // container.registerComponentImplementation(GPX.degreesType,DegreesTypeBinding.class);
        // container.registerComponentImplementation(GPX.dgpsStationType,DgpsStationTypeBinding.class);
        // container.registerComponentImplementation(GPX.emailType,EmailTypeBinding.class);
        // container.registerComponentImplementation(GPX.extensionsType,ExtensionsTypeBinding.class);
        // container.registerComponentImplementation(GPX.fixType,FixTypeBinding.class);
        container.registerComponentImplementation(GPX.gpxType,GpxTypeBinding.class);
        // container.registerComponentImplementation(GPX.latitudeType,LatitudeTypeBinding.class);
        // container.registerComponentImplementation(GPX.linkType,LinkTypeBinding.class);
        // container.registerComponentImplementation(GPX.longitudeType,LongitudeTypeBinding.class);
        // container.registerComponentImplementation(GPX.metadataType,MetadataTypeBinding.class);
        // container.registerComponentImplementation(GPX.personType,PersonTypeBinding.class);
        // container.registerComponentImplementation(GPX.ptsegType,PtsegTypeBinding.class);
        // container.registerComponentImplementation(GPX.ptType,PtTypeBinding.class);
        container.registerComponentImplementation(GPX.rteType,RteTypeBinding.class);
        container.registerComponentImplementation(GPX.rteptType,WptTypeBinding.class);
        container.registerComponentImplementation(GPX.trksegType,TrksegTypeBinding.class);
        container.registerComponentImplementation(GPX.trkType,TrkTypeBinding.class);
        container.registerComponentImplementation(GPX.trkptType,WptTypeBinding.class);
        container.registerComponentImplementation(GPX.wptType,WptTypeBinding.class);
    }
}
