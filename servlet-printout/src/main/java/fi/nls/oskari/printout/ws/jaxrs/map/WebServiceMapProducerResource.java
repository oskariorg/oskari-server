package fi.nls.oskari.printout.ws.jaxrs.map;

import com.vividsolutions.jts.geom.*;
import fi.nls.oskari.printout.config.ConfigValue;
import fi.nls.oskari.printout.input.geojson.MaplinkGeoJsonParser;
import fi.nls.oskari.printout.input.layers.MapLayerJSONParser;
import fi.nls.oskari.printout.input.maplink.MapLink;
import fi.nls.oskari.printout.input.maplink.MapLinkParser;
import fi.nls.oskari.printout.output.map.MapProducer;
import fi.nls.oskari.printout.output.map.MapProducerResource;
import fi.nls.oskari.printout.output.map.MetricScaleResolutionUtils;
import fi.nls.oskari.printout.printing.PDFProducer;
import fi.nls.oskari.printout.printing.PDFProducer.Options;
import fi.nls.oskari.printout.printing.PDFProducer.Page;
import fi.nls.oskari.printout.ws.jaxrs.format.*;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.request.RequestFilterException;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;
import java.util.Properties;

/*
 * 
 * This class is used in JAX-RS Resource class to implement map imaging .
 *
 * JAX-RS shares and instance of this class for any requests and this is assumed to be
 * threadsafe.
 * 
 * This class is also used in tests.
 * 
 * TBF: parameter handling has some serious design issues
 * 
 */
public class WebServiceMapProducerResource extends MapProducerResource {

    public WebServiceMapProducerResource(Properties props)
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException {
        super(props);

    }

    void addGeometryType(SimpleFeatureTypeBuilder typeBuilder, Geometry geometry) {
        typeBuilder.add("geometry", geometry != null ? geometry.getClass()
                : Geometry.class);
        typeBuilder.setDefaultGeometry("geometry");
    }

    SimpleFeatureBuilder createBuilder(CoordinateReferenceSystem crs,
            Geometry geometry, Integer targetWidth, Integer targetHeight,
            String featureName) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("feature");
        typeBuilder.setNamespaceURI("http://geotools.org");
        typeBuilder.setCRS(crs);

        if (geometry != null) {
            addGeometryType(typeBuilder, geometry);
        }

        typeBuilder.add("targetWidth",
                targetWidth != null ? targetWidth.getClass() : Object.class);
        typeBuilder.add("targetHeight",
                targetHeight != null ? targetHeight.getClass() : Object.class);
        typeBuilder.add("featureName",
                featureName != null ? featureName.getClass() : Object.class);

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(
                typeBuilder.buildFeatureType());
        builder.set("geometry", geometry);
        builder.set("targetWidth", targetWidth);
        builder.set("targetHeight", targetHeight);
        builder.set("featureName", featureName);

        return builder;
    }

    /**
     * gets DOCX map using parameters from JSON spec
     * 
     */
    public StreamingDOCXImpl getGeoJsonMapDOCX(InputStream inp,
            final Map<String, String> xClientInfo) throws IOException,
            ParseException, GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException,
            TransformException, InterruptedException,
            NoSuchAuthorityCodeException, FactoryException,
            com.vividsolutions.jts.io.ParseException,
            org.json.simple.parser.ParseException, URISyntaxException {

        MapProducer producer = fork(xClientInfo);

        TileLayer tileLayer = config.getTileLayer(producer.getTemplateLayer());
        GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

        MaplinkGeoJsonParser parser = new MaplinkGeoJsonParser();
        boolean isDebug = ConfigValue.GEOJSON_DEBUG.getConfigProperty(props,
                "false").equals("true");

        parser.setDebug(isDebug);

        Map<String, ?> root = parser.parse(inp);

        MapLayerJSONParser mapLayerJsonParser = new MapLayerJSONParser(props);

        MapLink mapLink = mapLayerJsonParser.parseMapLinkJSON(root, getGf(),
                gridSubset.getResolutions());

        Map<String, String> values = mapLink.getValues();

        Options opts = getPageOptions(values);
        opts.setContent(mapLink.getPrintoutContent());

        if (values.get("PAGESIZE") != null) {
            Page page = Page.valueOf(values.get("PAGESIZE"));
            int width = page.getMapWidthTargetInPoints(opts);
            int height = page.getMapHeightTargetInPoints(opts);
            values.put("WIDTH", Integer.toString(width, 10));
            values.put("HEIGHT", Integer.toString(height, 10));
            mapLink.setWidth(width);
            mapLink.setHeight(height);
        } else {
            mapLink.setWidth(Integer.valueOf(values.get("WIDTH"), 10));
            mapLink.setHeight(Integer.valueOf(values.get("HEIGHT"), 10));
        }

        mapLayerJsonParser.getMapLinkParser().validate(mapLink);

        StreamingDOCXImpl result = new StreamingDOCXImpl(producer, mapLink);

        result.underflow();
        return result;
    }

    /**
     * gets PDF map using parameters from JSON spec
     * 
     */
    public StreamingPDFImpl getGeoJsonMapPDF(InputStream inp,
            final Map<String, String> xClientInfo) throws IOException,
            ParseException, GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException,
            TransformException, InterruptedException,
            NoSuchAuthorityCodeException, FactoryException,
            com.vividsolutions.jts.io.ParseException,
            org.json.simple.parser.ParseException, URISyntaxException {

        MapProducer producer = fork(xClientInfo);
        TileLayer tileLayer = config.getTileLayer(producer.getTemplateLayer());
        GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

        MaplinkGeoJsonParser parser = new MaplinkGeoJsonParser();
        boolean isDebug = ConfigValue.GEOJSON_DEBUG.getConfigProperty(props,
                "false").equals("true");

        parser.setDebug(isDebug);

        Map<String, ?> root = parser.parse(inp);

        MapLayerJSONParser mapLayerJsonParser = new MapLayerJSONParser(props);

        MapLink mapLink = mapLayerJsonParser.parseMapLinkJSON(root, getGf(),
                gridSubset.getResolutions());

        Map<String, String> values = mapLink.getValues();
        Options opts = getPageOptions(values);
        opts.setContent(mapLink.getPrintoutContent());

        Page page = Page.valueOf(values.get("PAGESIZE"));

        int width = page.getMapWidthTargetInPoints(opts);
        int height = page.getMapHeightTargetInPoints(opts);

        values.put("WIDTH", Integer.toString(width, 10));
        values.put("HEIGHT", Integer.toString(height, 10));
        mapLink.setWidth(width);
        mapLink.setHeight(height);

        mapLayerJsonParser.getMapLinkParser().validate(mapLink);

        StreamingPDFImpl result = new StreamingPDFImpl(producer, mapLink, page,
                opts);

        result.underflow();
        return result;
    }

    /**
     * gets PNG Map using parameters from JSON spec
     * 
     */
    public StreamingPNGImpl getGeoJsonMapPNG(InputStream inp,
            final Map<String, String> xClientInfo) throws IOException,
            ParseException, GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException,
            TransformException, NoSuchAuthorityCodeException, FactoryException,
            com.vividsolutions.jts.io.ParseException,
            org.json.simple.parser.ParseException, URISyntaxException {

        MapProducer producer = fork(xClientInfo);
        TileLayer tileLayer = config.getTileLayer(producer.getTemplateLayer());
        GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

        MaplinkGeoJsonParser parser = new MaplinkGeoJsonParser();

        boolean isDebug = ConfigValue.GEOJSON_DEBUG.getConfigProperty(props,
                "false").equals("true");

        parser.setDebug(isDebug);

        Map<String, ?> root = parser.parse(inp);

        MapLayerJSONParser mapLayerJsonParser = new MapLayerJSONParser(props);

        MapLink mapLink = mapLayerJsonParser.parseMapLinkJSON(root, getGf(),
                gridSubset.getResolutions());

        Map<String, String> values = mapLink.getValues();
        Options opts = getPageOptions(values);
        opts.setContent(mapLink.getPrintoutContent());

        if (values.get("PAGESIZE") != null) {
            Page page = Page.valueOf(values.get("PAGESIZE"));

            int width = page.getMapWidthTargetInPoints(opts);
            int height = page.getMapHeightTargetInPoints(opts);

            values.put("WIDTH", Integer.toString(width, 10));
            values.put("HEIGHT", Integer.toString(height, 10));
            mapLink.setWidth(width);
            mapLink.setHeight(height);

        }

        /* fixes to help UI */
        if (values.get("SCALEDWIDTH") != null
                && values.get("SCALEDHEIGHT") == null) {

            /* calc based on WIDHT/HEIGHT */
            int targetScaledHeight = Integer.valueOf(values.get("SCALEDWIDTH"),
                    10)
                    * Integer.valueOf(values.get("HEIGHT"), 10)
                    / Integer.valueOf(values.get("WIDTH"), 10);

            values.put("SCALEDHEIGHT", Integer.toString(targetScaledHeight, 10));

        } else if (values.get("SCALEDWIDTH") == null
                && values.get("SCALEDHEIGHT") != null) {

            int targetScaledWidth = Integer.valueOf(values.get("SCALEDHEIGHT"),
                    10)
                    * Integer.valueOf(values.get("WIDTH"), 10)
                    / Integer.valueOf(values.get("HEIGHT"), 10);

            /* calc based on WIDHT/HEIGHT */
            values.put("SCALEDHEIGHT", Integer.toString(targetScaledWidth, 10));

        }

        mapLayerJsonParser.getMapLinkParser().validate(mapLink);

        StreamingPNGImpl result = new StreamingPNGImpl(producer, mapLink);
        result.underflow();

        return result;
    }

    /**
     * gets PPTX map using parameters from JSON spec
     * 
     */
    public StreamingPPTXImpl getGeoJsonMapPPTX(InputStream inp,
            final Map<String, String> xClientInfo) throws IOException,
            ParseException, GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException,
            TransformException, InterruptedException,
            NoSuchAuthorityCodeException, FactoryException,
            com.vividsolutions.jts.io.ParseException,
            org.json.simple.parser.ParseException, URISyntaxException {
        MapProducer producer = fork(xClientInfo);
        TileLayer tileLayer = config.getTileLayer(producer.getTemplateLayer());
        GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);
        MaplinkGeoJsonParser parser = new MaplinkGeoJsonParser();
        boolean isDebug = ConfigValue.GEOJSON_DEBUG.getConfigProperty(props,
                "false").equals("true");

        parser.setDebug(isDebug);

        Map<String, ?> root = parser.parse(inp);

        MapLayerJSONParser mapLayerJsonParser = new MapLayerJSONParser(props);

        MapLink mapLink = mapLayerJsonParser.parseMapLinkJSON(root, getGf(),
                gridSubset.getResolutions());

        Map<String, String> values = mapLink.getValues();
        Options opts = getPageOptions(values);
        opts.setContent(mapLink.getPrintoutContent());

        if (values.get("PAGESIZE") != null) {
            Page page = Page.valueOf(values.get("PAGESIZE"));

            int width = page.getMapWidthTargetInPoints(opts);
            int height = page.getMapHeightTargetInPoints(opts);

            values.put("WIDTH", Integer.toString(width, 10));
            values.put("HEIGHT", Integer.toString(height, 10));
            mapLink.setWidth(width);
            mapLink.setHeight(height);
        } else {
            mapLink.setWidth(Integer.valueOf(values.get("WIDTH"), 10));
            mapLink.setHeight(Integer.valueOf(values.get("HEIGHT"), 10));
        }

        mapLayerJsonParser.getMapLinkParser().validate(mapLink);

        StreamingPPTXImpl result = new StreamingPPTXImpl(producer, mapLink);

        result.underflow();
        return result;
    }

    /**
     * gets GeoJSON extent using values from JAX-RS GET request
     * 
     */
    public StreamingJSONImpl getMapExtentJSON(Map<String, String> values,
            final Map<String, String> xClientInfo) throws IOException,
            ParseException, GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException,
            TransformException, NoSuchAuthorityCodeException, FactoryException,
            com.vividsolutions.jts.io.ParseException {

        MapProducer producer = fork(xClientInfo);
        TileLayer tileLayer = config.getTileLayer(producer.getTemplateLayer());
        GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

        Page page = Page.valueOf(values.get("PAGESIZE"));
        String scaleResolverId = ConfigValue.SCALE_RESOLVER.getConfigProperty(
                props, "m_ol212");
        MapLinkParser mapLinkParser = new MapLinkParser(
                MetricScaleResolutionUtils.getScaleResolver(scaleResolverId),
                producer.getZoomOffset());

        MapLink mapLink = mapLinkParser.parseValueMapLink(values, layerJson,
                gf, gridSubset.getResolutions());

        Options opts = getPageOptions(values);
        opts.setContent(mapLink.getPrintoutContent());

        values.put("WIDTH",
                Integer.toString(page.getMapWidthTargetInPoints(opts), 10));
        values.put("HEIGHT",
                Integer.toString(page.getMapHeightTargetInPoints(opts), 10));

        mapLink.getValues().putAll(values);
        mapLinkParser.validate(mapLink);

        int width = page.getMapWidthTargetInPoints(opts);
        int height = page.getMapHeightTargetInPoints(opts);
        Point centre = mapLink.getCentre();
        Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
                centre, mapLink.getZoom(), width, height);

        /*
         * producer.getGsf().setEnvelope(env); Polygon extent =
         * producer.getGsf().createRectangle();
         */
        GeometryFactory gf = new GeometryFactory();
        Polygon extent = gf.createPolygon(
                gf.createLinearRing(new Coordinate[] {
                        new Coordinate(env.getMinX(), env.getMinY()),
                        new Coordinate(env.getMaxX(), env.getMinY()),
                        new Coordinate(env.getMaxX(), env.getMaxY()),
                        new Coordinate(env.getMinX(), env.getMaxY()),
                        new Coordinate(env.getMinX(), env.getMinY()) }), null);

        DefaultFeatureCollection features = new DefaultFeatureCollection(null,
                null);

        SimpleFeatureBuilder builder = createBuilder(producer.getCrs(), extent,
                width, height, "MapExtent");

        SimpleFeature f = builder.buildFeature("");
        features.add(f);

        mapLinkParser.validate(mapLink);

        StreamingJSONImpl result = new StreamingJSONImpl(features);

        return result;
    }

    /**
     * gets PDF map using parameters from JSON spec
     * 
     */
    public StreamingPDFImpl getMapPDF(InputStream inp,
            final Map<String, String> xClientInfo) throws IOException,
            ParseException, GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException,
            TransformException, InterruptedException,
            NoSuchAuthorityCodeException, FactoryException,
            com.vividsolutions.jts.io.ParseException, URISyntaxException {
        MapProducer producer = fork(xClientInfo);
        TileLayer tileLayer = config.getTileLayer(producer.getTemplateLayer());
        GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);
        MapLayerJSONParser mapLayerJsonParser = new MapLayerJSONParser(props);

        MapLink mapLink = mapLayerJsonParser.parseMapLinkJSON(inp, getGf(),
                gridSubset.getResolutions());

        Map<String, String> values = mapLink.getValues();
        Options opts = getPageOptions(values);
        opts.setContent(mapLink.getPrintoutContent());

        Page page = Page.valueOf(values.get("PAGESIZE"));

        int width = page.getMapWidthTargetInPoints(opts);
        int height = page.getMapHeightTargetInPoints(opts);

        values.put("WIDTH", Integer.toString(width, 10));
        values.put("HEIGHT", Integer.toString(height, 10));
        mapLink.setWidth(width);
        mapLink.setHeight(height);

        mapLayerJsonParser.getMapLinkParser().validate(mapLink);

        StreamingPDFImpl result = new StreamingPDFImpl(producer, mapLink, page,
                opts);

        result.underflow();
        return result;
    }

    /**
     * gets PDF snapshot using parameters from JAX-RS GET request
     * 
     */
    public StreamingPDFImpl getMapPDF(Map<String, String> values,
            final Map<String, String> xClientInfo) throws IOException,
            ParseException, GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException,
            TransformException, InterruptedException,
            NoSuchAuthorityCodeException, FactoryException,
            com.vividsolutions.jts.io.ParseException, URISyntaxException {
        MapProducer producer = fork(xClientInfo);
        TileLayer tileLayer = config.getTileLayer(producer.getTemplateLayer());
        GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);
        Page page = Page.valueOf(values.get("PAGESIZE"));

        String scaleResolverId = ConfigValue.SCALE_RESOLVER.getConfigProperty(
                props, "m_ol212");
        MapLinkParser mapLinkParser = new MapLinkParser(
                MetricScaleResolutionUtils.getScaleResolver(scaleResolverId),
                producer.getZoomOffset());

        MapLink mapLink = mapLinkParser.parseValueMapLink(values, layerJson,
                gf, gridSubset.getResolutions());

        Options opts = getPageOptions(values);
        opts.setContent(mapLink.getPrintoutContent());

        int width = page.getMapWidthTargetInPoints(opts);
        int height = page.getMapHeightTargetInPoints(opts);

        values.put("WIDTH", Integer.toString(width, 10));
        values.put("HEIGHT", Integer.toString(height, 10));

        mapLink.getValues().putAll(values);
        mapLink.setWidth(width);
        mapLink.setHeight(height);

        mapLinkParser.validate(mapLink);

        StreamingPDFImpl result = new StreamingPDFImpl(producer, mapLink, page,
                opts);

        result.underflow();
        return result;
    }

    /**
     * gets PNG map using parameters from JSON spec
     * 
     */
    public StreamingPNGImpl getMapPNG(InputStream inp,
            final Map<String, String> xClientInfo) throws IOException,
            ParseException, GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException,
            TransformException, NoSuchAuthorityCodeException, FactoryException,
            com.vividsolutions.jts.io.ParseException, URISyntaxException {
        MapProducer producer = fork(xClientInfo);
        TileLayer tileLayer = config.getTileLayer(producer.getTemplateLayer());
        GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

        MapLayerJSONParser mapLayerJsonParser = new MapLayerJSONParser(props);

        MapLink mapLink = mapLayerJsonParser.parseMapLinkJSON(inp, getGf(),
                gridSubset.getResolutions());

        Map<String, String> values = mapLink.getValues();
        Options opts = getPageOptions(values);
        opts.setContent(mapLink.getPrintoutContent());

        if (values.get("PAGESIZE") != null) {
            Page page = Page.valueOf(values.get("PAGESIZE"));
            int width = page.getMapWidthTargetInPoints(opts);
            int height = page.getMapHeightTargetInPoints(opts);
            values.put("WIDTH", Integer.toString(width, 10));
            values.put("HEIGHT", Integer.toString(height, 10));
            mapLink.setWidth(width);
            mapLink.setHeight(height);

        }

        /* fixes to help UI */
        if (values.get("SCALEDWIDTH") != null
                && values.get("SCALEDHEIGHT") == null) {

            /* calc based on WIDHT/HEIGHT */
            int targetScaledHeight = Integer.valueOf(values.get("SCALEDWIDTH"),
                    10)
                    * Integer.valueOf(values.get("HEIGHT"), 10)
                    / Integer.valueOf(values.get("WIDTH"), 10);

            values.put("SCALEDHEIGHT", Integer.toString(targetScaledHeight, 10));

        } else if (values.get("SCALEDWIDTH") == null
                && values.get("SCALEDHEIGHT") != null) {

            int targetScaledWidth = Integer.valueOf(values.get("SCALEDHEIGHT"),
                    10)
                    * Integer.valueOf(values.get("WIDTH"), 10)
                    / Integer.valueOf(values.get("HEIGHT"), 10);

            /* calc based on WIDHT/HEIGHT */
            values.put("SCALEDHEIGHT", Integer.toString(targetScaledWidth, 10));

        }

        String scaleResolverId = ConfigValue.SCALE_RESOLVER.getConfigProperty(
                props, "m_ol212");
        MapLinkParser mapLinkParser = new MapLinkParser(
                MetricScaleResolutionUtils.getScaleResolver(scaleResolverId),
                producer.getZoomOffset());

        mapLinkParser.validate(mapLink);

        StreamingPNGImpl result = new StreamingPNGImpl(producer, mapLink);
        result.underflow();

        return result;
    }

    /**
     * gets snapshot PNG using values from JAX-RS GET request
     * 
     */
    public StreamingPNGImpl getMapPNG(Map<String, String> values,
            final Map<String, String> xClientInfo) throws IOException,
            ParseException, GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException,
            TransformException, NoSuchAuthorityCodeException, FactoryException,
            com.vividsolutions.jts.io.ParseException, URISyntaxException {
        MapProducer producer = fork(xClientInfo);
        TileLayer tileLayer = config.getTileLayer(producer.getTemplateLayer());
        GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

        String scaleResolverId = ConfigValue.SCALE_RESOLVER.getConfigProperty(
                props, "m_ol212");
        MapLinkParser mapLinkParser = new MapLinkParser(
                MetricScaleResolutionUtils.getScaleResolver(scaleResolverId),
                producer.getZoomOffset());

        MapLink mapLink = mapLinkParser.parseValueMapLink(values, layerJson,
                gf, gridSubset.getResolutions());

        Options opts = getPageOptions(values);
        opts.setContent(mapLink.getPrintoutContent());

        if (values.get("PAGESIZE") != null) {
            Page page = Page.valueOf(values.get("PAGESIZE"));

            int width = page.getMapWidthTargetInPoints(opts);
            int height = page.getMapHeightTargetInPoints(opts);

            values.put("WIDTH", Integer.toString(width, 10));
            values.put("HEIGHT", Integer.toString(height, 10));

            mapLink.setWidth(width);
            mapLink.setHeight(height);
        }

        /* fixes to help UI */
        if (values.get("SCALEDWIDTH") != null
                && values.get("SCALEDHEIGHT") == null) {

            /* calc based on WIDHT/HEIGHT */
            int targetScaledHeight = Integer.valueOf(values.get("SCALEDWIDTH"),
                    10)
                    * Integer.valueOf(values.get("HEIGHT"), 10)
                    / Integer.valueOf(values.get("WIDTH"), 10);

            values.put("SCALEDHEIGHT", Integer.toString(targetScaledHeight, 10));

        } else if (values.get("SCALEDWIDTH") == null
                && values.get("SCALEDHEIGHT") != null) {

            int targetScaledWidth = Integer.valueOf(values.get("SCALEDHEIGHT"),
                    10)
                    * Integer.valueOf(values.get("WIDTH"), 10)
                    / Integer.valueOf(values.get("HEIGHT"), 10);

            /* calc based on WIDHT/HEIGHT */
            values.put("SCALEDHEIGHT", Integer.toString(targetScaledWidth, 10));

        }

        mapLink.getValues().putAll(values);
        mapLinkParser.validate(mapLink);

        StreamingPNGImpl result = new StreamingPNGImpl(producer, mapLink);
        result.underflow();

        return result;
    }

    /**
     * gets snapshot PPTX using values from JAX-RS GET request
     * 
     */
    public StreamingOutput getMapPPTX(Map<String, String> values,
            final Map<String, String> xClientInfo)
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException,
            com.vividsolutions.jts.io.ParseException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException, URISyntaxException {

        MapProducer producer = fork(xClientInfo);
        TileLayer tileLayer = config.getTileLayer(producer.getTemplateLayer());
        GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);
        Page page = Page.valueOf(values.get("PAGESIZE"));
        String scaleResolverId = ConfigValue.SCALE_RESOLVER.getConfigProperty(
                props, "m_ol212");
        MapLinkParser mapLinkParser = new MapLinkParser(
                MetricScaleResolutionUtils.getScaleResolver(scaleResolverId),
                producer.getZoomOffset());

        MapLink mapLink = mapLinkParser.parseValueMapLink(values, layerJson,
                gf, gridSubset.getResolutions());

        Options opts = getPageOptions(values);
        opts.setContent(mapLink.getPrintoutContent());

        int width = page.getMapWidthTargetInPoints(opts);
        int height = page.getMapHeightTargetInPoints(opts);

        values.put("WIDTH", Integer.toString(width, 10));
        values.put("HEIGHT", Integer.toString(height, 10));
        mapLink.getValues().putAll(values);
        mapLink.setWidth(width);
        mapLink.setHeight(height);

        mapLinkParser.validate(mapLink);

        StreamingPPTXImpl result = new StreamingPPTXImpl(producer, mapLink);

        result.underflow();
        return result;

    }

    /**
     * maps request parameters to PDF parameters
     * 
     * @param values
     * @param mapLink
     * @return
     */
    private Options getPageOptions(Map<String, String> values) {
        PDFProducer.Options pageOptions = new PDFProducer.Options();
        pageOptions.setPageTitle(values.get("PAGETITLE"));
        pageOptions.setPageDate(values.get("PAGEDATE") != null ? values.get(
                "PAGEDATE").equals("true") : false);
        pageOptions.setPageScale(values.get("PAGESCALE") != null ? values.get(
                "PAGESCALE").equals("true") : false);
        pageOptions.setPageLogo(values.get("PAGELOGO") != null ? values.get(
                "PAGELOGO").equals("true") : false);
        if (pageOptions.isPageLogo()) {
            String pageLogoResource = ConfigValue.MAPPRODUCER_LOGO_RESOURCE
                    .getConfigProperty(props, "logo.png");

            pageOptions.setPageLogoResource(pageLogoResource);
        }

        pageOptions.setPageLegend(values.get("PAGELEGEND") != null ? values
                .get("PAGELEGEND").equals("true") : false);
        pageOptions.setPageCopyleft(values.get("PAGECOPYLEFT") != null ? values
                .get("PAGECOPYLEFT").equals("true") : false);
        pageOptions.setPageTemplate(values.get("PAGETEMPLATE"));
        if (values.get("PAGEMAPRECT") != null) {
            pageOptions.setPageMapRectFromString(values.get("PAGEMAPRECT"));
        }

        return pageOptions;
    }

}
