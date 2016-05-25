package fi.nls.oskari.work.fe;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.geotools.sld.SLDConfiguration;
import org.geotools.styling.*;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FEStyledLayerDescriptorManager {

    protected static final Logger log = LogFactory
            .getLogger(FEStyledLayerDescriptorManager.class);
    /**
     * Parses SLD style from a String (XML)
     *
     * @param xml
     * @return sld
     */
    /*
     * SLD handling building and caching
     */
    final static Map<String, Style> templateSLD = new ConcurrentHashMap<String, Style>();

    public static Style createSLDStyle(String sldFilename) {

        log.debug("[fe] Creating Style tryin 1.1.0 " + sldFilename);
        try {
            final java.net.URL surl = FEMapLayerJob.class
                    .getResource(sldFilename);
            org.geotools.sld.v1_1.SLDConfiguration configuration = new org.geotools.sld.v1_1.SLDConfiguration() {
                protected void configureContext(
                        org.picocontainer.MutablePicoContainer container) {
                    container.registerComponentImplementation(
                            StyleFactory.class, StyleFactoryImpl.class);

                    DefaultResourceLocator locator = new DefaultResourceLocator();
                    locator.setSourceUrl(surl);
                    container.registerComponentInstance(ResourceLocator.class,
                            locator);
                };
            };
            Parser parser = new Parser(configuration);

            StyledLayerDescriptor sld = null;

            try {
                sld = (StyledLayerDescriptor) parser.parse(surl.openStream());

                for (int i = 0; i < sld.getStyledLayers().length; i++) {
                    Style[] styles = null;

                    if (sld.getStyledLayers()[i] instanceof NamedLayer) {
                        NamedLayer layer = (NamedLayer) sld.getStyledLayers()[i];
                        styles = layer.getStyles();
                    } else if (sld.getStyledLayers()[i] instanceof UserLayer) {
                        UserLayer layer = (UserLayer) sld.getStyledLayers()[i];
                        styles = layer.getUserStyles();
                    } else {
                        log.debug("[fe] --> "
                                + sld.getStyledLayers()[i].getClass());
                    }

                    if (styles != null) {
                        for (int j = 0; j < styles.length; j++) {
                            Style s = styles[j];

                            if (s.featureTypeStyles() != null
                                    && s.featureTypeStyles().size() > 0) {

                                if (s.featureTypeStyles().get(0)
                                        .featureTypeNames().size() > 0) {
                                    log.debug("[fe] --> RESETTING and USING "
                                            + styles[j].getClass());
                                    s.featureTypeStyles().get(0)
                                            .featureTypeNames().clear();
                                } else {
                                    log.debug("[fe] --> #1 USING "
                                            + styles[j].getClass());
                                }

                                return s;
                            } else if (!(s instanceof NamedStyle)) {
                                log.debug("[fe] --> #2 USING "
                                        + styles[j].getClass());
                                return s;
                            } else {
                                log.debug("[fe] --> ? " + s);
                            }

                        }
                    }

                }
            } catch (Exception ex) {
                log.debug("[fe] SLD FALLBACK required " + ex);
            }

            log.debug("[fe] -- FALLBACK Creating Style tryin 1.0.0 "
                    + sldFilename);
            /*
             * static protected Style createSLDStyle(InputStream xml) {
             */
            Configuration config = new SLDConfiguration();

            parser = new Parser(config);
            sld = null;

            sld = (StyledLayerDescriptor) parser.parse(FEMapLayerJob.class
                    .getResourceAsStream(sldFilename));

            Style style = SLD.styles(sld)[0];
            log.debug("[fe] - Using 1.0.0 Style " + style);
            return style;

        } catch (Exception ee) {
            ee.printStackTrace(System.err);

        }

        return null;
    }

    public static Style createSLDStyle(InputStream xml) {
        if(xml == null){
            return null;
        }

        Configuration config = new SLDConfiguration();
        Parser parser = new Parser(config);
        StyledLayerDescriptor sld = null;
        try {
            sld = (StyledLayerDescriptor) parser.parse(xml);
        } catch (Exception e) {
            log.debug(e + "Failed to create SLD Style");

            return null;
        }
        return SLD.styles(sld)[0];
    }

    /**
     * Sld style could be path to sdl resource file or sld string content
     * @param sldName
     * @param sldPath
     * @return
     */
    public static Style getSLD(String sldName, String sldPath) {
        //Path key
        Style sld = templateSLD.get(sldPath);

        if (sld != null) {
            log.debug("[fe] using cached SLD for " + sldPath);
            return sld;
        }
        // Try name key
        if (sldName != null) {
            sld = templateSLD.get(sldName);
        }

        if (sld != null) {
            log.debug("[fe] using cached SLD for " + sldName);
            return sld;
        }
        if (sldPath.substring(0, 30).indexOf("<") > -1) {
            log.debug("[fe] creating SLD style for " + sldName);
            InputStream stream = null;
            try {
                stream = new ByteArrayInputStream(sldPath.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error(e, "Encoding error in sld style streaming");
            }

            sld = createSLDStyle(stream);

        } else {
            log.debug("[fe] creating SLD from " + sldPath);

            sld = createSLDStyle(sldPath);
        }

        if (sld != null) {
            log.debug("[fe] created and cached SLD for " + sldPath);
            templateSLD.put(sldPath, sld);
        }
        return sld;
    }

}
