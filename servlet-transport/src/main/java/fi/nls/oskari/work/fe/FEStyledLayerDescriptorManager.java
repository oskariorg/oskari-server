package fi.nls.oskari.work.fe;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.geotools.sld.SLDConfiguration;
import org.geotools.styling.DefaultResourceLocator;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.NamedStyle;
import org.geotools.styling.ResourceLocator;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryImpl;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.UserLayer;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

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
     * 
     * @param sldPath
     * @param layerId
     * @return
     */
    public static Style getSLD(String sldPath) {
        Style sld = templateSLD.get(sldPath);

        if (sld != null) {
            log.debug("[fe] using cached SLD for " + sldPath);
            return sld;
        }
        log.debug("[fe] creating SLD from " + sldPath);

        sld = createSLDStyle(sldPath);
        if (sld != null) {
            log.debug("[fe] created and cached SLD for " + sldPath);
            templateSLD.put(sldPath, sld);
        }
        return sld;
    }

}
