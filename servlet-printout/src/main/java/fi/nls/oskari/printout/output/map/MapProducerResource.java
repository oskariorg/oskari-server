package fi.nls.oskari.printout.output.map;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.grid.GridSetBroker;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import fi.nls.oskari.printout.config.ConfigValue;
import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.input.layers.MapLayerJSON;
import fi.nls.oskari.printout.input.layers.MapLayerJSONParser;

/* 
 *
 * This base class manages resources that are assumed to be threadsafe.
 * An instance of this class is shared for any map printout request.
 * 
 * see also subclass WebServiceMapProducerResource for map operations.
 * 
 */
public abstract class MapProducerResource {

    protected String epsgCode = null;
    protected String gridSubsetName = null;

    protected Properties props;

    URL layerJSONurl = getClass().getResource("blank-layers.json");

    boolean layersDirty = true;
    private static Log log = LogFactory.getLog(MapProducerResource.class);

    private GridSetBroker gridSetBroker;

    protected XMLConfiguration config;

    protected MapLayerJSONParser layerJsonParser;
    protected MapLayerJSON layerJson = null;

    protected final GeometryFactory gf = new GeometryFactory();

    GeometricShapeFactory gsf = new GeometricShapeFactory();

    private CoordinateReferenceSystem crs;
    SimpleFeatureTypeBuilder ftb;

    SimpleFeatureType schema;

    private String gridResource;

    private Integer zoomOffset = 0;

    public MapProducerResource(Properties props) throws IOException,
            GeoWebCacheException, NoSuchAuthorityCodeException,
            FactoryException {
        this.props = props;
        layerJsonParser = new MapLayerJSONParser(props);

        buildZoomOffset();

        buildGridSets();
        buildCrs();
        buildSchema();

        log.info("MapResource instantiated");
    }

    private void buildZoomOffset() {
        zoomOffset = ConfigValue.MAPLINK_ZOOM_OFFSET
                .getConfigProperty(props, 0);
    }

    private void buildCrs() throws IOException, NoSuchAuthorityCodeException,
            FactoryException {
        epsgCode = ConfigValue.EPSGCODE.getConfigProperty(props, "EPSG:3067");

        log.warn("Using EPSG code " + epsgCode);

        crs = CRS.decode(epsgCode);

        log.warn("Mapped EPSG code to CRS " + crs);
    }

    private void buildGridSets() throws IOException, GeoWebCacheException {
        this.gridSubsetName = ConfigValue.GRIDSUBSETNAME.getConfigProperty(
                props, "EPSG_3067_MML");

        this.gridResource = ConfigValue.GRIDRESOURCE.getConfigProperty(props);

        URL source = null;
        if (this.gridResource != null) {
            source = new URL(this.gridResource);
        } else {
            source = MapProducer.class.getResource("geowebcache_template.xml");
        }
        /*
         * File configFileTemplate = File.createTempFile("geowebcache", ".xml");
         * configFile = new File(configFileTemplate.getParent(),
         * "geowebcache.xml"); FileUtils.copyURLToFile(source, configFile);
         */

        gridSetBroker = new GridSetBroker(true, false);
        InputStream inp = source.openStream();
        try {
            config = new XMLConfiguration(inp);// null,
                                               // configFile.getParent());
        } finally {
            inp.close();
        }

        config.initialize(gridSetBroker);

    }

    private void buildSchema() {
        ftb = new SimpleFeatureTypeBuilder();
        // set the name
        ftb.setName("Tile");
        ftb.add("serial", Integer.class);
        // add some properties
        ftb.add("minScale", Double.class);
        ftb.add("maxScale", Double.class);
        ftb.add("style", String.class);

        // add a geometry property
        ftb.setCRS(crs); // set crs first
        ftb.add("geom", Polygon.class, crs); // then add geometry
        ftb.add("url", String.class);
        ftb.add("env", Envelope.class);
        ftb.add("width", Integer.class);
        ftb.add("height", Integer.class);
        ftb.add("cacheable", Boolean.class);
        ftb.add("credentials", String.class);
        ftb.add("layertype", String.class);
        ftb.add("cookie", String.class);

        schema = ftb.buildFeatureType();

    }

    public MapProducer fork(final Map<String, String> xClientInfo)
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException {

        loadLayerJson();

        MapProducer producer = new MapProducer(this, gridSubsetName, epsgCode,
                xClientInfo, props);
        producer.setSfb(new SimpleFeatureBuilder(schema));
        return producer;
    }

    public XMLConfiguration getConfig() {
        return config;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public SimpleFeatureTypeBuilder getFtb() {
        return ftb;
    }

    public GeometryFactory getGf() {
        return gf;
    }

    public GridSetBroker getGridSetBroker() {
        return gridSetBroker;
    }

    public GeometricShapeFactory getGsf() {
        return gsf;
    }

    public MapLayerJSON getLayerJson() {
        return layerJson;
    }

    public URL getLayerJSONurl() {
        return layerJSONurl;
    }

    public Properties getProps() {
        return props;
    }

    public SimpleFeatureType getSchema() {
        return schema;
    }

    public boolean isLayersDirty() {
        return layersDirty;
    }

    public void loadLayerJson() throws IOException, ParseException {

        if (layerJson == null || layersDirty) {
            log.info("Loading Layer List from " + layerJSONurl);

            Map<String, LayerDefinition> layerDefs = layerJsonParser
                    .parse(layerJSONurl);
            layerJson = new MapLayerJSON(layerDefs);

            layersDirty = false;

            log.info("Loaded Layer List");
        }
    }

    public void setConfig(XMLConfiguration config) {
        this.config = config;
    }

    public void setCrs(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public void setFtb(SimpleFeatureTypeBuilder ftb) {
        this.ftb = ftb;
    }

    public void setGridSetBroker(GridSetBroker gridSetBroker) {
        this.gridSetBroker = gridSetBroker;
    }

    public void setGsf(GeometricShapeFactory gsf) {
        this.gsf = gsf;
    }

    public void setLayerJson(MapLayerJSON layerJson) {
        this.layerJson = layerJson;
    }

    public void setLayerJSONurl(URL layerJSONurl) {
        this.layerJSONurl = layerJSONurl;
    }

    public void setLayersDirty(boolean layersDirty) {
        this.layersDirty = layersDirty;
    }

    public void setSchema(SimpleFeatureType schema) {
        this.schema = schema;
    }

    public Integer getZoomOffset() {
        return zoomOffset;
    }

}
