package fi.nls.oskari.geoserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Feign;
import feign.Logger;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

/**
 * Created by SMAKINEN on 1.9.2015.
 * http://docs.geoserver.org/stable/en/user/rest/
 * http://code.google.com/p/gsrcj/source/browse/trunk/src/main/java/org/geopublishing/GsRest.java
 */
public class GeoserverTest {
    @Test
    @Ignore
    public void testSequenceForExample() throws Exception {
        // https://github.com/Netflix/feign/
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        Geoserver geoserver = Feign.builder()
                .decoder(new JacksonDecoder(mapper))
                .encoder(new JacksonEncoder(mapper))
                .logger(new Logger.JavaLogger())
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(new BasicAuthRequestInterceptor("admin", "geoserver"))
                .target(Geoserver.class, "http://localhost:8080/geoserver/rest");

        // https://github.com/geoserver/geoserver/blob/master/src/restconfig/src/test/java/org/geoserver/catalog/rest/WorkspaceTest.java
        // Creating a namespace (creates a workspace with uri)
        // (with ws you can only give a name, with ns you can also provide the uri)
        Namespace ns = new Namespace();
        ns.prefix = "oskari";
        ns.uri = "http://www.oskari.org";
        geoserver.createNamespace(ns);

        DBDatastore ds = new DBDatastore();
        ds.name = "oskariStore";
        ds.enabled = true;
        ds.connectionParameters.user = "postgres";
        ds.connectionParameters.database = "oskaridb";
        ds.connectionParameters.passwd = "admin";
        ds.connectionParameters.namespace = "oskari";
        ds.addEntry("schema", "public");
        ds.addEntry("Loose bbox", "true");
        System.out.println(mapper.writeValueAsString(ds));
        geoserver.createDBDatastore(ds, ds.connectionParameters.namespace);


        FeatureType type = new FeatureType();
        type.enabled = true;
        type.name = "categories";
        type.srs = "EPSG:3067";

        geoserver.createFeatureType(type, "oskari", "oskariStore");

        FeatureType type2 = new FeatureType();
        type2.enabled = true;
        type2.name = "my_places_categories";
        type2.srs = "EPSG:3067";

        geoserver.createFeatureType(type2, "oskari", "oskariStore");

        final String xml = readString(GeoserverTest.class.getResourceAsStream("default.sld")).trim();
        geoserver.createSLD("Style", xml);
        geoserver.linkStyleToLayer("Style", "my_places_categories", "moi");
        geoserver.setDefaultStyleForLayer("Style", "my_places_categories", "oskari");
    }

    /**
     * Reads the given input stream and converts its contents to a string using given charset
     * @param is
     * @return
     * @throws IOException
     */
    public static String readString(InputStream is)
            throws IOException {
        if (is == null) {
            return "";
        }

        final Writer writer = new StringWriter();
        final char[] buffer = new char[1024];
        try {
            final Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }
}
