package org.oskari.service.mvt.wfs;


import javax.xml.namespace.QName;

import org.eclipse.xsd.util.XSDSchemaLocator;
import org.geotools.gml3.GMLConfiguration;
import org.picocontainer.MutablePicoContainer;

public class OskariGMLConfiguration extends GMLConfiguration {

    private final String user;
    private final String pass;

    public OskariGMLConfiguration() {
        this(null, null);
    }

    public OskariGMLConfiguration(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public void configureContext(final MutablePicoContainer container) {
        super.configureContext(container);
        XSDSchemaLocator locator = new OskariCachingSchemaLocator(user, pass);
        // No idea what the key is used for
        QName key = new QName("mycustom", "schemaLocator");
        container.registerComponentInstance(key, locator);
    }

}
