package fi.nls.oskari.fe.output.format.png.geotools;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.AbstractOutputStreamProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.sld.SLDConfiguration;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* PoC which builds GeoTools Map to PNG */
public class MapContentOutputProcessor extends AbstractOutputStreamProcessor
        implements OutputProcessor {

    protected static final Logger log = LogFactory.getLogger(MapContentOutputProcessor.class);

    public static Style createSLDStyle(String sldFilename) {

        log.info("[fe] Creating Style tryin 1.1.0 " + sldFilename);
        try {
            final java.net.URL surl = MapContentOutputProcessor.class
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

            StyledLayerDescriptor sld = (StyledLayerDescriptor) parser
                    .parse(surl.openStream());

            for (int i = 0; i < sld.getStyledLayers().length; i++) {
                Style[] styles = null;

                if (sld.getStyledLayers()[i] instanceof NamedLayer) {
                    NamedLayer layer = (NamedLayer) sld.getStyledLayers()[i];
                    styles = layer.getStyles();
                } else if (sld.getStyledLayers()[i] instanceof UserLayer) {
                    UserLayer layer = (UserLayer) sld.getStyledLayers()[i];
                    styles = layer.getUserStyles();
                } else {
                    log.info("[fe] --> " + sld.getStyledLayers()[i].getClass());
                }

                if (styles != null) {
                    for (int j = 0; j < styles.length; j++) {
                        Style s = styles[j];

                        if (s.featureTypeStyles() != null
                                && s.featureTypeStyles().size() > 0) {

                            if (s.featureTypeStyles().get(0).featureTypeNames()
                                    .size() > 0) {
                                log.info("[fe] --> RESETTING and USING "
                                        + styles[j].getClass());
                                s.featureTypeStyles().get(0).featureTypeNames()
                                        .clear();
                            } else {
                                log.info("[fe] --> #1 USING "
                                        + styles[j].getClass());
                            }

                            return s;
                        } else if (!(s instanceof NamedStyle)) {
                            log.info("[fe] --> #2 USING "
                                    + styles[j].getClass());
                            return s;
                        } else {
                            log.info("[fe] --> ? " + s);
                        }

                    }
                }

            }

            log.info("[fe] -- FALLBACK Creating Style tryin 1.0.0 "
                    + sldFilename);
            /*
             * static protected Style createSLDStyle(InputStream xml) {
             */
            Configuration config = new SLDConfiguration();

            parser = new Parser(config);
            sld = null;

            sld = (StyledLayerDescriptor) parser
                    .parse(MapContentOutputProcessor.class
                            .getResourceAsStream(sldFilename));

            Style style = SLD.styles(sld)[0];
            log.info("[fe] - Using 1.0.0 Style " + style);
            return style;

        } catch (Exception ee) {
            log.error(ee, "Error creating SLD style");
        }

        return null;
    }

    private CoordinateReferenceSystem crs;
    private SimpleFeatureType schema;
    private SimpleFeatureBuilder sfb;
    private List<SimpleFeature> sfc;
    private StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    private FilterFactory filterFactory = CommonFactoryFinder
            .getFilterFactory2();
    private String srsName = null;

    private Style sldStyle = null;

    protected MapContentOutputProcessor() {

    }

    public MapContentOutputProcessor(String srsName) {
        this.srsName = srsName;
    }

    public MapContentOutputProcessor(String srsName, Style sldStyle) {
        this.srsName = srsName;
        this.sldStyle = sldStyle;
    }

    public void begin() throws IOException {
        // TODO Auto-generated method stub
        /* Setup MAP */

        try {
            crs = CRS.decode(srsName, true);
        } catch (NoSuchAuthorityCodeException e) {
            log.warn(e, "Couldn't setup CoordinateReferenceSystem");
            throw new IOException(e);
        } catch (FactoryException e) {
            log.warn(e, "Couldn't setup CoordinateReferenceSystem");
            throw new IOException(e);
        }

        Resource type = Resource.iri("http://test/", "Feature");

        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName(type.getLocalPart());
        ftb.setNamespaceURI(type.getNs());

        // add a geometry property
        ftb.setCRS(crs); // set crs first
        ftb.add("geometry", Geometry.class, crs); // then add geometry

        schema = ftb.buildFeatureType();

        sfb = new SimpleFeatureBuilder(schema);

        sfc = new LinkedList<SimpleFeature>();

    }

    private Style createStyle() {

        // create a partially opaque outline stroke
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLUE), filterFactory.literal(1),
                filterFactory.literal(0.5));

        // create a partial opaque fill
        Fill fill = styleFactory.createFill(filterFactory.literal(Color.CYAN),
                filterFactory.literal(0.5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        PolygonSymbolizer polygonSymbolizer = styleFactory
                .createPolygonSymbolizer(stroke, fill, null);

        /* Point */
        Graphic gr = styleFactory.createDefaultGraphic();

        Mark mark = styleFactory.getStarMark();

        mark.setStroke(styleFactory.createStroke(
                filterFactory.literal(Color.BLUE), filterFactory.literal(1)));

        mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add(mark);
        gr.setSize(filterFactory.literal(15));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        PointSymbolizer pointSymbolizer = styleFactory.createPointSymbolizer(
                gr, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(polygonSymbolizer);
        rule.symbolizers().add(pointSymbolizer);

        FeatureTypeStyle fts = styleFactory
                .createFeatureTypeStyle(new Rule[] { rule });
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    public void edge(Resource subject, Resource predicate, Resource value)
            throws IOException {
        /* Links -> might draw something */

    }

    public void end() throws IOException {
        /* Draw MAP */
        if (crs == null) {
            throw new IOException("No CRS");
        }
        
        if( sfc.isEmpty()) {
            return;
        }
        
        final MapContent map = new MapContent();
        final MapViewport viewport = new MapViewport();
        viewport.setCoordinateReferenceSystem(crs);

        boolean upnorth = crs.getCoordinateSystem().getAxis(1).getDirection() == AxisDirection.NORTH;

        map.setViewport(viewport);
        final StreamingRenderer draw = new StreamingRenderer();
        draw.setMapContent(map);

        final SimpleFeatureCollection collection = DataUtilities
                .collection(sfc);

        ReferencedEnvelope bounds = collection.getBounds();
        
        if( bounds.isEmpty() ) {
            return;
        }
        // bounds.expandBy(-100);

        Style style = sldStyle != null ? sldStyle : createStyle();
        Layer layer = new FeatureLayer(collection, style);
        map.addLayer(layer);

        int width = 500;
        int height = upnorth ? (int) (bounds.getHeight() / bounds.getWidth() * width)
                : (int) (bounds.getWidth() / bounds.getHeight() * width);

        final Rectangle rect = new Rectangle(0, 0, width, height);

        viewport.setScreenArea(rect);
        viewport.setBounds(bounds);

        /*
         * AffineTransform transform = null;
         * 
         * transform = RendererUtilities.worldToScreenTransform(bounds, rect,
         * crs);
         */
        viewport.setCoordinateReferenceSystem(crs);

        BufferedImage image = null;
        Rectangle outputArea = new Rectangle(0, 0, width, height);

        /* upscale / downscale to help geotools... */

        image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        draw.paint(g2d, outputArea, bounds);

        g2d.dispose();

        BufferedOutputStream pngo = new BufferedOutputStream(outs);
        ImageIO.write(image, "png", pngo);
        pngo.flush();

        map.dispose();
    }

    public void flush() throws IOException {

    }

    public void type(Resource type,
            List<Pair<Resource, XSDDatatype>> simpleProperties,
            List<Pair<Resource, Object>> linkProperties,
            List<Pair<Resource, String>> geometryProperties) throws IOException {

    }

    public void vertex(final Resource iri, final Resource type,
            final List<Pair<Resource, Object>> simpleProperties,
            final List<Pair<Resource, Object>> linkProperties)
            throws IOException {
        /* No Geometry - IGNORE */

    }

    public void vertex(Resource iri, Resource type,
            List<Pair<Resource, Object>> simpleProperties,
            List<Pair<Resource, Object>> linkProperties,
            List<Pair<Resource, Geometry>> geometryProperties)
            throws IOException {

        for (Pair<Resource, Geometry> geomPair : geometryProperties) {
            Geometry geom = geomPair.getValue();

            log.debug("+",geom.toString());

            sfb.add(geom);

            SimpleFeature f = sfb.buildFeature(iri.toString());

            sfc.add(f);

        }

    }
    public void merge(List<JSONObject> list, Resource res) {


    }
    public void equalizePropertyArraySize(Map<String,Integer> multiElemmap,  Map<String, Resource> resmap) {

    }

}
