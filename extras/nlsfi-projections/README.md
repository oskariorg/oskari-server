# Projection transform helper for Finnish projections

Wraps a library for Finnish coordinate system transforms so it's usable with Oskari-server. 
The compiled lib is available in 
oskari.org [releases Maven repository](http://oskari.org/nexus/content/repositories/releases) (no source available):

    <dependency>
        <groupId>fi.nls.projektio</groupId>
        <artifactId>java-projections</artifactId>
        <version>2012.0.0</version>
    </dependency>
        
See fi.nls.oskari.NLSFIProjections class for supported projections.

Configured for use in oskari-ext.properties with:

    projection.library.class=fi.nls.oskari.NLSFIPointTransformer

With the property the `Coordinates` action route uses this library as default transform. 