package fi.nls.oskari.printout.ws.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

import fi.nls.oskari.printout.ws.jaxrs.resource.MapResource;

@ApplicationPath("/")
public class Application extends javax.ws.rs.core.Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register resources and features
        classes.add(MapResource.class);
        classes.add(MultiPartFeature.class);

        return classes;
    }
}