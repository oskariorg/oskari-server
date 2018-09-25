package org.oskari.service.mvt.wfs;


import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.eclipse.xsd.util.XSDSchemaLocator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OskariCachingSchemaLocator implements XSDSchemaLocator {

    private static final Logger LOG = LogFactory.getLogger(OskariCachingSchemaLocator.class);
    private static final Map<String, XSDSchema> CACHE = new ConcurrentHashMap<String, XSDSchema>();

    private final String username;
    private final String password;

    public OskariCachingSchemaLocator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public XSDSchema locateSchema(
            XSDSchema schema,
            String namespaceURI,
            String rawSchemaLocationURI,
            String resolvedSchemaLocationURI) {
        return CACHE.computeIfAbsent(rawSchemaLocationURI, uri -> parseSchema(uri, username, password));
    }

    protected static XSDSchema parseSchema(String rawSchemaLocationURI, String username, String password) {
        try {
            HttpURLConnection conn = IOHelper.getConnection(rawSchemaLocationURI, username, password);
            byte[] response = IOHelper.readBytes(conn);
            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xsd", new org.eclipse.xsd.util.XSDResourceFactoryImpl());
            XSDResourceImpl xsdMainResource = (XSDResourceImpl) resourceSet.createResource(URI.createURI(".xsd"));
            xsdMainResource.load(new ByteArrayInputStream(response), resourceSet.getLoadOptions());
            return xsdMainResource.getSchema();
        } catch (IOException e) {
            LOG.warn(e, "Failed to locate schema:", rawSchemaLocationURI);
            return null;
        }
    }

}
