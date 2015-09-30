package fi.nls.oskari.printout.output.map;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.filter.request.RequestFilterException;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import fi.nls.oskari.printout.breeding.ChangeSet;
import fi.nls.oskari.printout.breeding.ChangeSetEntry;
import fi.nls.oskari.printout.breeding.WorkingSet;
import fi.nls.oskari.printout.breeding.breeder.IChangeSetEntryTransaction;
import fi.nls.oskari.printout.breeding.breeder.IWorkingSetTileBreeder.StatusChangeCallBack;
import fi.nls.oskari.printout.breeding.maplink.MapLinkWorkingSetProcessor;
import fi.nls.oskari.printout.breeding.maplink.MapLinkWorkingSetTileBreeder;
import fi.nls.oskari.printout.config.ConfigValue;
import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.output.layer.AsyncDirectTileLayer;
import fi.nls.oskari.printout.output.layer.AsyncLayerProcessor;
import fi.nls.oskari.printout.output.layer.DirectFeatureLayer;
import fi.nls.oskari.printout.output.layer.DirectTileLayer;

/* 
 *
 * This class manages resources that are assumed to be non-threadsafe.
 * An instance of this class is forked for each map printout request.
 * 
 */
public class MapProducer {

    public enum ImageType {

        ARGB(BufferedImage.TYPE_4BYTE_ABGR), RGB(BufferedImage.TYPE_3BYTE_BGR)

        ;
        private int imageType;

        ImageType(int imageType) {
            this.imageType = imageType;
        }

        public int getImageType() {
            return imageType;
        }
    }

    private static Log log = LogFactory.getLog(MapProducer.class);
    private XMLConfiguration config;
    private GridSetBroker gridSetBroker;
    private String templateLayer = "EPSG_3067_MML_LAYER_TEMPLATE";
    private String gridSubsetName;
    private CoordinateReferenceSystem crs;
    private String epsgCode;
    private MapLinkWorkingSetProcessor processor;
    private MapLinkWorkingSetTileBreeder breeder;
    private long timeoutInSeconds = 16;

    WKTReader wktReader = new WKTReader();
    SimpleFeatureType schema;

    private SimpleFeatureBuilder sfb;

    GeometricShapeFactory gsf;
    MapContent map;
    MapViewport viewport;

    GTRenderer draw;

    private Map<String, String> xClientInfo;

    protected Properties props;

    protected boolean useCache;

    protected Integer zoomOffset;

    protected StatusChangeCallBack NULL_CALLBACK = new StatusChangeCallBack() {

        public void noteChange(WorkingSet workingSet, ChangeSet changeSet,
                ChangeSetEntry changeSetEntry,
                IChangeSetEntryTransaction transaction) {

        }

        public void noteChange(WorkingSet workingSet, ChangeSet changeSet,
                IChangeSetEntryTransaction transaction) {

        }

        public void noteChange(WorkingSet workingSet,
                IChangeSetEntryTransaction transaction) {

        }

        public void noteException(WorkingSet workingSet, ChangeSet changeSet,
                ChangeSetEntry changeSetEntry,
                IChangeSetEntryTransaction transaction, Exception x) {

        }

    };

    public MapProducer(MapProducerResource resource, String gsn, String epsg,
            Map<String, String> xClientInfo, Properties props)
            throws IOException, GeoWebCacheException,
            NoSuchAuthorityCodeException, FactoryException,
            com.vividsolutions.jts.io.ParseException {
        this.xClientInfo = xClientInfo;

        this.crs = resource.getCrs();
        this.gridSetBroker = resource.getGridSetBroker();
        this.config = resource.getConfig();
        this.gsf = resource.getGsf();
        this.schema = resource.getSchema();
        this.props = props;
        this.zoomOffset = resource.getZoomOffset();

        if (ConfigValue.LAYER_TIMEOUT_SECONDS.getConfigProperty(props) != null) {
            timeoutInSeconds = Long.valueOf(
                    ConfigValue.LAYER_TIMEOUT_SECONDS.getConfigProperty(props),
                    10);
        }

        if (ConfigValue.LAYER_TEMPLATE.getConfigProperty(props) != null) {
            templateLayer = ConfigValue.LAYER_TEMPLATE.getConfigProperty(props);
        }

        useCache = ConfigValue.REDIS_BLOB_CACHE
                .getConfigProperty(props, "true").equals("true");

        gridSubsetName = gsn;
        epsgCode = epsg;

        processor = new MapLinkWorkingSetProcessor();
        processor.setConfig(config);
        processor.setGridSetBroker(gridSetBroker);
        processor.setTemplateLayer(templateLayer);

        processor.setTileLayer(config.getTileLayer(templateLayer));
        processor.setGridSubset(processor.getTileLayer().getGridSubset(
                gridSubsetName));

        breeder = new MapLinkWorkingSetTileBreeder();
        breeder.setConfig(config);
        breeder.setGridSetBroker(gridSetBroker);
        breeder.setCallback(NULL_CALLBACK);

        if (ConfigValue.MAPPRODUCER_MAXEXTENT.getConfigProperty(props) != null) {
            Geometry maxExtent = wktReader
                    .read(ConfigValue.MAPPRODUCER_MAXEXTENT
                            .getConfigProperty(props));
            breeder.setMaxExtent(maxExtent);
            processor.setMaxExtent(maxExtent);
        }

        map = new MapContent();
        viewport = new MapViewport();
        viewport.setCoordinateReferenceSystem(crs);
        map.setViewport(viewport);
        draw = new StreamingRenderer();
        draw.setMapContent(map);

    }

    protected void buildLayers(
            Vector<DirectTileLayer> mapLayers,
            final List<LayerDefinition> layers,
            final Envelope env,
            final ReferencedEnvelope bounds,
            AffineTransform transform,
            Map<String, FeatureCollection<SimpleFeatureType, SimpleFeature>> fcs,
            Vector<FeatureCollection<SimpleFeatureType, SimpleFeature>> fcList,
            AsyncLayerProcessor asyncProc) throws IOException,
            URISyntaxException {

        for (final LayerDefinition layerDefinition : layers) {

            Geometry polygon = layerDefinition.getGeom();
            /*
             * complex to test - we should know env srs - layer definition geom
             * srs is WGS84 if (polygon != null) { Envelope envGeom =
             * polygon.getEnvelopeInternal(); if (!envGeom.intersects(env)) {
             * 
             * log.info("Out of bounds for " + layerDefinition.getLayerid() +
             * " / bounds " + env. + " vs " + envGeom); continue; } }
             */

            if ("geojson".equals(layerDefinition.getLayerType())) {
                log.info("geojson Processing for Layer "
                        + layerDefinition.getLayerid());
                /* vector layer from embedded geojson */

                if (layerDefinition.getData() != null
                        && layerDefinition.getData().size() > 0) {
                    CollectionDataStore ds = new CollectionDataStore(
                            layerDefinition.getData());
                    SimpleFeatureSource fs = ds.getFeatureSource(ds
                            .getTypeNames()[0]);

                    DirectFeatureLayer layer = new DirectFeatureLayer(
                            layerDefinition, fs, transform);
                    layer.setLayerOpacity(layerDefinition.getOpacity());
                    map.addLayer(layer);
                }

            } else if (layerDefinition.getTiles() != null) {
                log.info("TILES Processing for Layer "
                        + layerDefinition.getLayerid());

                /* PNG layers with predefined tiles */
                DefaultFeatureCollection fc = new DefaultFeatureCollection(
                        layerDefinition.getLayerid(), schema);
                fcs.put(layerDefinition.getLayerid(), fc);
                fcList.add(fc);

                DirectTileLayer layer = new AsyncDirectTileLayer(
                        layerDefinition, asyncProc, fc, transform, xClientInfo,
                        timeoutInSeconds, useCache);

                layer.setLayerOpacity(layerDefinition.getOpacity());

                buildPredefinedTiles(layerDefinition, layer, fc);
                mapLayers.add(layer);
                map.addLayer(layer);

            } else if (layerDefinition.getLayerType().equals("wfslayer")) {
                /*
                 * if ("false"
                 * .equals(ConfigValue.LAYER_URLTEMPLATE_WFSLAYER_LEGACY
                 * .getConfigProperty(props, "false"))) {
                 */
                log.info("Assuming TILES for WFS Layer - Legacy support OFF "
                        + layerDefinition.getLayerid());
                continue;
            } else if (layerDefinition.isSingleTile()) {
                log.warn("NYI Single Tile Processing for Layer "
                        + layerDefinition.getLayerid());
                continue;

            } else {

                log.info("Default Processing for Layer "
                        + layerDefinition.getLayerid());

                /* PNG layers of various kinds */
                DefaultFeatureCollection fc = new DefaultFeatureCollection(
                        layerDefinition.getLayerid(), schema);
                fcs.put(layerDefinition.getLayerid(), fc);
                fcList.add(fc);

                DirectTileLayer layer = new AsyncDirectTileLayer(
                        layerDefinition, asyncProc, fc, transform, xClientInfo,
                        timeoutInSeconds, useCache);

                layer.setLayerOpacity(layerDefinition.getOpacity());

                mapLayers.add(layer);
                map.addLayer(layer);
            }

        }

    }

    protected void buildLayerTiles(final Vector<DirectTileLayer> mapLayers,
            final List<LayerDefinition> layers, final Envelope env,
            final int zoom) throws ParseException, IOException,
            GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException {
        /* breed tiles */
        processor.setGridSubsetName(gridSubsetName);

        breeder.setTileProcessor(new MapLinkWorkingSetTileBreeder.MapLinkTileProcessor() {
            int n = 0;

            public void processTile(Envelope e, Polygon p, int tw, int th,
                    Map<String, String> parameters, String templateLayerName,
                    long[] gridLoc) throws IOException {

                processLayersTiles(mapLayers, e, tw, th, parameters,
                        templateLayerName, gridLoc, ++n);
            }

        });

        List<WorkingSet> workingSets = processor.getWorkingSetsForMapLink(env,
                zoom, layers);

        for (WorkingSet ws : workingSets) {
            for (ChangeSet cs : ws.getChangeSets()) {

                for (ChangeSetEntry cse : cs.getChangeSetEntries()) {
                    breeder.processChangeSetEntry(ws, cs, cse);
                }
            }
        }

    }

    protected void processLayersTiles(final Vector<DirectTileLayer> mapLayers,
            Envelope e, int tw, int th, Map<String, String> parameters,
            String templateLayerName, long[] gridLoc, int n) throws IOException {

        for (final DirectTileLayer layer : mapLayers) {
            LayerDefinition layerDefinition = layer.getLayerDefinition();

            /* if predefined tiles - skip */
            if (layerDefinition.getTiles() != null) {
                log.debug("Tile Layer - No URL processing for "
                        + layer.getLayerDefinition().getLayerid());
                continue;
            }

            final DefaultFeatureCollection fc = layer.getFc();
            final TileLayer tileLayer = processor.getTileLayer();

            final String url = buildLayerURL(layerDefinition, e, tw, th,
                    gridLoc, tileLayer);
            final String cookie = buildLayerCookie(layerDefinition, url);

            if (url == null) {
                log.debug("No URL for " + layerDefinition.getLayerid());
                continue;
            }

            final SimpleFeature tf = createTileFeature(layerDefinition, url,
                    cookie, e, tw, th, n);
            fc.add(tf);

        }

    }

    protected boolean isLocalUrl(String url) {
        final String localUrl = ConfigValue.MAPPRODUCER_LOCALURL_MATCH
                .getConfigProperty(props);

        if (localUrl == null || localUrl.isEmpty()) {
            return false;
        }

        return url.startsWith(localUrl);
    }

    protected String fixLocalUrl(String url) {
        final String localUrlPrefix = ConfigValue.MAPPRODUCER_LOCALURL_PREFIX
                .getConfigProperty(props);

        if (localUrlPrefix == null || localUrlPrefix.isEmpty()) {
            return url;
        }

        return localUrlPrefix + url;
    }

    protected String buildLayerCookie(LayerDefinition layerDefinition,
            String url) {

        if (!isLocalUrl(layerDefinition.getWmsurl())) {
            return null;
        }

        return xClientInfo.get("Cookie");
    }

    /**
     * 
     * @param layerDefinition
     * @param e
     * @param tw
     * @param th
     * @param tileIndex
     * @param tileLayer
     * @return
     * @throws IOException
     */
    String buildLayerURL(LayerDefinition layerDefinition, Envelope e, int tw,
            int th, long[] tileIndex, TileLayer tileLayer) throws IOException {
        String layerUrl = layerDefinition.getWmsurl();

        String layersParam = layerDefinition.getWmsname();
        String style = layerDefinition.getStyle();

        String layerType = layerDefinition.getLayerType();
        String version = layerDefinition.getWmsVersion();

        String separator = layerUrl.indexOf('?') != -1 ? "&" : "?";
        String url = null;

        if (layerType.equals("wmslayer")) {

            String format = layerDefinition.getFormat();
            if (format == null) {
                format = "image/png";
            }

            if (isLocalUrl(layerUrl)) {
                layerUrl = fixLocalUrl(layerUrl);
            }

            /* might use some geotools class here to build WMS query */
            url = layerUrl
                    + separator
                    + "SERVICE=WMS"
                    + "&VERSION="
                    + "1.1.1"
                    //+ version
                    + "&REQUEST=GetMap" + "&WIDTH=" + tw + "&HEIGHT=" + th
                    + "&FORMAT=" + URLEncoder.encode(format, "UTF-8")
                    + "&STYLES="
                    + (style != null ? URLEncoder.encode(style, "UTF-8") : "")
                    + "&LAYERS=" + layersParam + "&SRS="
                    + URLEncoder.encode(epsgCode, "UTF-8")
                    + "&TRANSPARENT=TRUE" + "&BBOX=" + e.getMinX() + ","
                    + e.getMinY() + "," + e.getMaxX() + "," + e.getMaxY();

        } else if (layerType.equals("wfslayer")) {
            String format = layerDefinition.getFormat();

            /* this will be loaded via http proxy from action route */
            url = layerUrl
                    + separator
                    + "SERVICE=WMS"
                    + "&VERSION="
                    + "1.1.1"
                    + version 
                    + "&REQUEST=GetMap" + "&WIDTH=" + tw + "&HEIGHT=" + th
                    + "&FORMAT=" + URLEncoder.encode(format, "UTF-8")
                    + "&STYLES="
                    + (style != null ? URLEncoder.encode(style, "UTF-8") : "")
                    + "&LAYERS=" + layersParam + "&SRS="
                    + URLEncoder.encode(epsgCode, "UTF-8")
                    + "&TRANSPARENT=TRUE" + "&BBOX=" + e.getMinX() + ","
                    + e.getMinY() + "," + e.getMaxX() + "," + e.getMaxY();

        } else if (layerType.equals("statslayer")) {

            /* this will be loaded via http proxy from action route */
            url = layerUrl;

        } else if (layerType.equals("wmtslayer")) {

            /* To DO */
            /* This assumes map compatible tilematrixset for any WMTS layers atm */
            /*
             * Next Steps: - kvp or rest selection based on configuration -
             * support for layer specific grid sets requires separate set of
             * tiles for each layer using known identified gridset configuration
             * - support for layer specific grid sets requires separate set of
             * tiles for each layer using gridset setup read from json
             * configuration
             */

            /* map tileIndex to WMTS parameters */
            GridSubset gridSubset = tileLayer.getGridSubset(processor
                    .getGridSubsetName());

            long x = tileIndex[0];
            long y = tileIndex[1];
            long z = tileIndex[2];

            long[] gridCov = gridSubset.getCoverage((int) z);
            final long tilesHigh = gridSubset.getNumTilesHigh((int) z);

            if (x < gridCov[0] || x > gridCov[2]) {
                throw new IOException("TileOutOfRange : TILECOLUMN Column " + x
                        + " is out of range, min: " + gridCov[0] + " max:"
                        + gridCov[2]);
            }

            // String[] gridNames = gridSubset.getGridNames();
            String tileMatrix = Long.toString(z, 10);// gridNames[(int) z];

            String tileCol = Long.toString(x, 10);
            String tileRow = Long.toString(tilesHigh - 1 - y, 10);

            if (y < gridCov[1] || y > gridCov[3]) {
                long minRow = tilesHigh - gridCov[3] - 1;
                long maxRow = tilesHigh - gridCov[1] - 1;

                throw new IOException("TileOutOfRange : TILEROW Row " + tileRow
                        + " is out of range, min: " + minRow + " max:" + maxRow);
            }

            String tileMatrixSet = processor.getGridSubsetName();

            String urlTemplate = layerDefinition.getUrlTemplate();
            if (urlTemplate == null) {
                log.debug("WMTS no urlTemplate assuming KVP");

                String format = layerDefinition.getFormat();
                if (format == null) {
                    format = "image/png";
                }
                String[] gridNames = gridSubset.getGridNames();
                url = layerUrl
                        + separator
                        + "SERVICE=WMTS"
                        + "&VERSION=" + version
                        + "&REQUEST=GetTile"
                        + "&FORMAT=" + URLEncoder.encode(format, "UTF-8")
                        + "&STYLE=" + (style != null ? URLEncoder.encode(style, "UTF-8") : "")
                        + "&LAYER=" + layersParam
                        + "&TILEMATRIXSET=" + tileMatrixSet
                        + "&TILEMATRIX=" + URLEncoder.encode(gridNames[(int) z], "UTF-8")
                        + "&TILEROW=" + tileRow
                        + "&TILECOL=" + tileCol;
                // default to png
                /*
                String imageTypeExtension = "png";
                String format = layerDefinition.getFormat();
                if (format != null) {
                    imageTypeExtension = format.substring(format.indexOf('/') + 1);
                }*/

                /* REST */
                /* WMTS 1.0.0 support only */
                /*
                 * This will be replaced by content from
                 * resourceUrl/tile/template
                 */
                /*
                String wmtsRestPart = "1.0.0" + "/" + layersParam + "/" + style
                        + "/" + tileMatrixSet + "/" + tileMatrix + "/"
                        + tileRow + "/" + tileCol + "." + imageTypeExtension;

                url = layerUrl + "/" + wmtsRestPart;
                */
            } else {
                log.debug("WMTS with urlTemplate");
                // "template":
                // "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/taustakartta/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png",
                url = urlTemplate
                        .replaceFirst("\\{TileMatrixSet\\}", tileMatrixSet)
                        .replaceFirst("\\{TileMatrix\\}", tileMatrix)
                        .replaceFirst("\\{TileRow\\}", tileRow)
                        .replaceFirst("\\{TileCol\\}", tileCol);
            }

            /* KVP to be implemented */
            /*
             * String wmtsKvpPart =
             * "?service=WMTS&request=GetTile&version=1.0.0" + "&layer=" +
             * layersParam + "&style=" + style + "&format=image/" +
             * imageTypeExtension + "&TileMatrixSet=" + tileMatrixSet +
             * "&TileMatrix=" + tileMatrix + "&TileRow=" + tileRow + "&TileCol="
             * + tileCol;
             * 
             * url = layerUrl + wmtsKvpPart;
             */
        }

        return url;
    }

    BufferedImage buildMapImage(final int width, final int height,
            final ReferencedEnvelope bounds, final ImageType imageType,
            final Envelope crop, final AffineTransform transform) {
        BufferedImage image = null;
        Rectangle outputArea = new Rectangle(0, 0, width, height);

        /* upscale / downscale to help geotools... */

        image = new BufferedImage(width, height, imageType.getImageType());
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        draw.paint(g2d, outputArea, bounds);

        g2d.dispose();

        if (crop != null) {
            double[] srcPts = new double[] { crop.getMinX(), crop.getMinY(),
                    crop.getMaxX(), crop.getMaxY() };
            double[] dstPts = new double[] { 0.0, 0.0, 0.0, 0.0 };

            transform.transform(srcPts, 0, dstPts, 0, 2);

            Envelope envAdj = new Envelope(dstPts[0], dstPts[2], dstPts[1],
                    dstPts[3]);
            int tw = Double.valueOf(envAdj.getWidth()).intValue();
            int th = Double.valueOf(envAdj.getHeight()).intValue();

            log.info("CROPPING " + envAdj);

            BufferedImage cropped = image.getSubimage((int) envAdj.getMinX(),
                    (int) envAdj.getMinY(), tw, th);
            image.flush();
            image = cropped;

        }

        return image;
    }

    protected void buildPredefinedTiles(LayerDefinition layerDefinition,
            DirectTileLayer layer, DefaultFeatureCollection fc) {

        String urlWhiteList = ConfigValue.LAYER_TILES_URL_WHITELIST
                .getConfigProperty(props);

        int n = 0;
        for (Map<String, ?> predefinedTile : layerDefinition.getTiles()) {
            ++n;

            String url = (String) predefinedTile.get("url");

            if (url == null) {
                log.warn("NULL URL for " + layerDefinition.getLayerid());
                continue;
            }

            if (!url.matches(urlWhiteList)) {
                log.warn("WHITELIST MISMATCH " + url);
                continue;
            }

            List<?> bboxList = (List<?>) predefinedTile.get("bbox");

            if (bboxList == null) {
                log.warn("NULL BBOX  for " + layerDefinition.getLayerid());
                continue;
            }

            if (bboxList.size() < 4) {
                log.warn("Invalid BBOX  for " + layerDefinition.getLayerid());
                continue;
            }

            double x1 = ((Number) bboxList.get(0)).doubleValue();
            double y1 = ((Number) bboxList.get(1)).doubleValue();
            double x2 = ((Number) bboxList.get(2)).doubleValue();
            double y2 = ((Number) bboxList.get(3)).doubleValue();

            Envelope e = new Envelope(x1, x2, y1, y2);

            if (layerDefinition.getLayerType().equals("statslayer")) {

                String tileUrl = layerDefinition.getWmsurl() + url;

                fc.add(createTileFeature(layerDefinition, tileUrl, null, e,
                        Integer.MIN_VALUE, Integer.MIN_VALUE, n));

            } else {

                fc.add(createTileFeature(layerDefinition, url, null, e,
                        Integer.MIN_VALUE, Integer.MIN_VALUE, n));
            }

        }

    }

    protected SimpleFeature createTileFeature(
            final LayerDefinition layerDefinition, final String url,
            final String cookie, Envelope e, int tw, int th, int n) {
        gsf.setEnvelope(e);
        Polygon geom = gsf.createRectangle();
        sfb.add(new Integer(n));
        sfb.add(layerDefinition.getMinScale());
        sfb.add(layerDefinition.getMaxScale());
        sfb.add(layerDefinition.getStyle());
        sfb.add(geom);
        sfb.add(url);
        sfb.add(e);
        sfb.add(Integer.valueOf(tw));
        sfb.add(Integer.valueOf(th));
        sfb.add(Boolean.valueOf(layerDefinition.isCacheable()));
        sfb.add(layerDefinition.getCredentials());
        sfb.add(layerDefinition.getLayerType());
        sfb.add(cookie);

        SimpleFeature feature = sfb.buildFeature("f." + n);
        return feature;

    }

    public MapLinkWorkingSetTileBreeder getBreeder() {
        return breeder;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public GeometricShapeFactory getGsf() {
        return gsf;
    }

    public BufferedImage getMap(AsyncLayerProcessor asyncProc,
            final Envelope env, final int zoom, final int width,
            final int height, final List<LayerDefinition> layers,
            ImageType imageType) throws ParseException, IOException,
            GeoWebCacheException, XMLStreamException,
            FactoryConfigurationError, RequestFilterException,
            TransformException, URISyntaxException {
        return getMap(asyncProc, env, zoom, width, height, layers, imageType,
                null);
    }

    public BufferedImage getMap(AsyncLayerProcessor asyncProc,
            final Envelope env, final int zoom, final int width,
            final int height, final List<LayerDefinition> layers,
            final ImageType imageType, final Envelope crop)
            throws ParseException, IOException, GeoWebCacheException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException, URISyntaxException {

        BufferedImage image = null;
        /* setup */

        final ReferencedEnvelope bounds = new ReferencedEnvelope(env.getMinX(),
                env.getMaxX(), env.getMinY(), env.getMaxY(), crs);

        final Rectangle rect = new Rectangle(0, 0, width, height);

        viewport.setScreenArea(rect);
        viewport.setBounds(bounds);

        final Vector<FeatureCollection<SimpleFeatureType, SimpleFeature>> fcList = new Vector<FeatureCollection<SimpleFeatureType, SimpleFeature>>();
        final Vector<DirectTileLayer> mapLayers = new Vector<DirectTileLayer>();

        final AffineTransform transform = RendererUtilities
                .worldToScreenTransform(bounds, rect, crs);

        final Map<String, FeatureCollection<SimpleFeatureType, SimpleFeature>> fcs = new HashMap<String, FeatureCollection<SimpleFeatureType, SimpleFeature>>();

        try {
            buildLayers(mapLayers, layers, env, bounds, transform, fcs, fcList,
                    asyncProc);
            buildLayerTiles(mapLayers, layers, env, zoom);

            image = buildMapImage(width, height, bounds, imageType, crop,
                    transform);

        } finally {
            for (Layer layer : mapLayers) {
                layer.preDispose();
                map.removeLayer(layer);
            }
            map.dispose();
        }

        return image;

    }

    public MapLinkWorkingSetProcessor getProcessor() {
        return processor;
    }

    public SimpleFeatureType getSchema() {
        return schema;
    }

    public SimpleFeatureBuilder getSfb() {
        return sfb;
    }

    public String getTemplateLayer() {
        return templateLayer;
    }

    public void setBreeder(MapLinkWorkingSetTileBreeder breeder) {
        this.breeder = breeder;
    }

    public void setProcessor(MapLinkWorkingSetProcessor processor) {
        this.processor = processor;
    }

    public void setSchema(SimpleFeatureType schema) {
        this.schema = schema;
    }

    public void setSfb(SimpleFeatureBuilder sfb) {
        this.sfb = sfb;
    }

    public void setTemplateLayer(String templateLayer) {
        this.templateLayer = templateLayer;
    }

    public Integer getZoomOffset() {
        return zoomOffset;
    }

}