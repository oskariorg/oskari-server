package org.oskari.service.wfs.client;
import javax.xml.namespace.QName;

import org.eclipse.xsd.util.XSDSchemaLocator;
import org.geotools.wfs.v2_0.WFSConfiguration;
import org.picocontainer.MutablePicoContainer;

public class OskariWFS2Configuration extends WFSConfiguration {

    private final String user;
    private final String pass;

    public OskariWFS2Configuration() {
        this(null, null);
    }

    public OskariWFS2Configuration(String user, String pass) {
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