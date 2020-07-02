package org.geotools.gpx.gpx10;

import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;

/**
 * Parser configuration for the http://www.topografix.com/GPX/1/1 schema.
 *
 * @generated
 */
public class GPX10Configuration extends Configuration {

    /**
     * Creates a new configuration.
     *
     * @generated
     */
    public GPX10Configuration() {
        super(GPX10.getInstance());

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
        container.registerComponentImplementation(GPX10.gpxType,GpxTypeBinding.class);
        container.registerComponentImplementation(GPX10.gpx,GpxTypeBinding.class);
        // container.registerComponentImplementation(GPX.latitudeType,LatitudeTypeBinding.class);
        // container.registerComponentImplementation(GPX.linkType,LinkTypeBinding.class);
        // container.registerComponentImplementation(GPX.longitudeType,LongitudeTypeBinding.class);
        // container.registerComponentImplementation(GPX.metadataType,MetadataTypeBinding.class);
        // container.registerComponentImplementation(GPX.personType,PersonTypeBinding.class);
        // container.registerComponentImplementation(GPX.ptsegType,PtsegTypeBinding.class);
        // container.registerComponentImplementation(GPX.ptType,PtTypeBinding.class);
        container.registerComponentImplementation(GPX10.rteType,RteTypeBinding.class);
        container.registerComponentImplementation(GPX10.rteptType,WptTypeBinding.class);
        container.registerComponentImplementation(GPX10.trksegType,TrksegTypeBinding.class);
        container.registerComponentImplementation(GPX10.trkType,TrkTypeBinding.class);
        container.registerComponentImplementation(GPX10.trkptType,WptTypeBinding.class);
        container.registerComponentImplementation(GPX10.wptType,WptTypeBinding.class);
    }
}