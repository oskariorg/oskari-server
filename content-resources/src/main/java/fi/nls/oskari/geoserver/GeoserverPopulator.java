package fi.nls.oskari.geoserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Feign;
import feign.FeignException;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import fi.nls.oskari.db.ConnectionInfo;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

/**
 * Created by SMAKINEN on 1.9.2015.
 */
public class GeoserverPopulator {

    public static final String NAMESPACE = "oskari";
    public static final String DEFAULT_SRS = "EPSG:3067";
    private static final Logger LOG = LogFactory.getLogger(GeoserverPopulator.class);

    public static void setupAll() throws Exception {
        setupAll(DEFAULT_SRS);
    }
    public static void setupAll(final String srs) throws Exception {
        setupMyplaces(srs);
        setupAnalysis(srs);
        setupUserlayers(srs);
    }

    private static Geoserver getGeoserver(String module) {

        final String geoserverBaseUrl = PropertyUtil.getNecessary("geoserver." + module  + ".url"); // http://localhost:8080/geoserver
        final String geoserverUser = PropertyUtil.getNecessary("geoserver." + module  + ".user"); // admin
        final String geoserverPasswd = PropertyUtil.getNecessary("geoserver." + module  + ".password"); // geoserver

        // https://github.com/Netflix/feign/wiki/Custom-error-handling
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        return Feign.builder()
                .decoder(new JacksonDecoder(mapper))
                .encoder(new JacksonEncoder(mapper))
                .logger(new feign.Logger.JavaLogger())
                .logLevel(feign.Logger.Level.FULL)
                .requestInterceptor(new BasicAuthRequestInterceptor(geoserverUser, geoserverPasswd))
                .target(Geoserver.class, geoserverBaseUrl + "/rest");
    }

    public static void setupMyplaces(final String srs) throws Exception {
        final String module = "myplaces";
        Geoserver geoserver = getGeoserver(module);

        // Creating a namespace creates a workspace
        // (with ws you can only give a name, with ns you can also provide the uri)
        Namespace ns = new Namespace();
        try {

            ns.prefix = NAMESPACE;
            ns.uri = "http://www.oskari.org";
            geoserver.createNamespace(ns);
            LOG.info("Added namespace:", ns);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding namespace");
        }

        final String storeName = module;
        try {
            DBDatastore ds = new DBDatastore();
            ds.name = storeName;


            DatasourceHelper helper = DatasourceHelper.getInstance();
            ConnectionInfo info = helper.getPropsForDS(module);

            ds.connectionParameters.user = info.user;
            ds.connectionParameters.passwd = info.pass;
            ds.connectionParameters.database = info.getDBName();
            // in 2.5.2 namespace = NAMESPACE, in 2.7.1 it needs to be the uri?
            ds.connectionParameters.namespace = ns.uri;
            ds.addEntry("Loose bbox", "true");
            //System.out.println(mapper.writeValueAsString(ds));
            geoserver.createDBDatastore(ds, NAMESPACE);
            LOG.info("Added store:", ds);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding store");
        }

        // for data modification (WFS) - layers
        try {
            FeatureType featureCategories = new FeatureType();
            featureCategories.enabled = true;
            featureCategories.name = "categories";
            featureCategories.srs = srs;

            geoserver.createFeatureType(featureCategories, NAMESPACE, storeName);
            LOG.info("Added featuretype:", featureCategories);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding featuretype categories");
        }

        // for data modification (WFS) - places
        try {
            FeatureType featurePlaces = new FeatureType();
            featurePlaces.enabled = true;
            featurePlaces.name = "my_places";
            featurePlaces.srs = srs;

            geoserver.createFeatureType(featurePlaces, NAMESPACE, storeName);
            LOG.info("Added featuretype:", featurePlaces);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding featuretype my_places");
        }

        // for viewing the places (WMS) - combination for layers/places
        FeatureType featurePlacesCategories = new FeatureType();
        try {
            featurePlacesCategories.enabled = true;
            featurePlacesCategories.name = "my_places_categories";
            featurePlacesCategories.srs = srs;

            geoserver.createFeatureType(featurePlacesCategories, NAMESPACE, storeName);
            LOG.info("Added featuretype:", featurePlacesCategories);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding featuretype my_places_categories");
        }

        final String sld = IOHelper.readString(GeoserverPopulator.class.getResourceAsStream("/sld/myplaces/MyPlacesDefaultStyle.sld"));
        final String sldName = "MyPlacesDefaultStyle";
        try {
            geoserver.createSLD(sldName, sld);
            LOG.info("Added SLD:", sldName);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding SLD");
        }
        try {
            geoserver.linkStyleToLayer(sldName, featurePlacesCategories.name, NAMESPACE);
            LOG.info("Linked SLD", sldName, " to layer", featurePlacesCategories.name, "in namespace", NAMESPACE);
        } catch (FeignException ex) {
            LOG.error(ex, "Error linking SLD");
        }
        geoserver.setDefaultStyleForLayer(sldName, featurePlacesCategories.name, NAMESPACE);
        LOG.info("Set default SLD", sldName, " for layer", featurePlacesCategories.name, "in namespace", NAMESPACE);
    }

    public static void setupAnalysis(final String srs) throws Exception {
        final String module = "analysis";
        Geoserver geoserver = getGeoserver(module);

        // Creating a namespace creates a workspace
        // (with ws you can only give a name, with ns you can also provide the uri)
        Namespace ns = new Namespace();
        try {

            ns.prefix = NAMESPACE;
            ns.uri = "http://www.oskari.org";
            geoserver.createNamespace(ns);
            LOG.info("Added namespace:", ns);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding namespace");
        }

        final String storeName = module;
        try {
            DBDatastore ds = new DBDatastore();
            ds.name = storeName;

            DatasourceHelper helper = DatasourceHelper.getInstance();
            ConnectionInfo info = helper.getPropsForDS(module);

            ds.connectionParameters.user = info.user;
            ds.connectionParameters.passwd = info.pass;
            ds.connectionParameters.database = info.getDBName();
            // !! in 2.5.2 namespace = NS PREFIX, in 2.7.1 it needs to be the NS URI!!
            ds.connectionParameters.namespace = ns.uri;
            ds.addEntry("Loose bbox", "true");
            //System.out.println(mapper.writeValueAsString(ds));
            geoserver.createDBDatastore(ds, NAMESPACE);
            LOG.info("Added store:", ds);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding store");
        }

        // for data modification (WFS) - data
        try {
            FeatureType featureData = new FeatureType();
            featureData.enabled = true;
            featureData.name = "analysis_data";
            featureData.srs = srs;

            geoserver.createFeatureType(featureData, NAMESPACE, storeName);
            LOG.info("Added featuretype:", featureData);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding featuretype analysis_data");
        }

        // for viewing (WMS) - combination for data/style
        FeatureType featureStyledData = new FeatureType();
        try {
            featureStyledData.enabled = true;
            featureStyledData.name = "analysis_data_style";
            featureStyledData.srs = srs;

            geoserver.createFeatureType(featureStyledData, NAMESPACE, storeName);
            LOG.info("Added featuretype:", featureStyledData);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding featuretype analysis_data_style");
        }

        final String sld = IOHelper.readString(GeoserverPopulator.class.getResourceAsStream("/sld/analysis/AnalysisDefaultStyle.sld"));
        final String sldName = "AnalysisDefaultStyle";
        try {
            geoserver.createSLD(sldName, sld);
            LOG.info("Added SLD:", sldName);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding SLD");
        }
        try {
            geoserver.linkStyleToLayer(sldName, featureStyledData.name, NAMESPACE);
            LOG.info("Linked SLD", sldName, " to layer", featureStyledData.name, "in namespace", NAMESPACE);
        } catch (FeignException ex) {
            LOG.error(ex, "Error linking SLD");
        }
        geoserver.setDefaultStyleForLayer(sldName, featureStyledData.name, NAMESPACE);
        LOG.info("Set default SLD", sldName, " for layer", featureStyledData.name, "in namespace", NAMESPACE);
    }


    public static void setupUserlayers(final String srs) throws Exception {
        final String module = "userlayer";
        Geoserver geoserver = getGeoserver(module);

        // Creating a namespace creates a workspace
        // (with ws you can only give a name, with ns you can also provide the uri)
        Namespace ns = new Namespace();
        try {

            ns.prefix = NAMESPACE;
            ns.uri = "http://www.oskari.org";
            geoserver.createNamespace(ns);
            LOG.info("Added namespace:", ns);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding namespace");
        }

        final String storeName = module;
        try {
            DBDatastore ds = new DBDatastore();
            ds.name = storeName;

            DatasourceHelper helper = DatasourceHelper.getInstance();
            ConnectionInfo info = helper.getPropsForDS(module);

            ds.connectionParameters.user = info.user;
            ds.connectionParameters.passwd = info.pass;
            ds.connectionParameters.database = info.getDBName();
            // !! in 2.5.2 namespace = NS PREFIX, in 2.7.1 it needs to be the NS URI!!
            ds.connectionParameters.namespace = ns.uri;
            ds.addEntry("Loose bbox", "true");
            ds.addEntry("Primary key metadata table", "gt_pk_metadata_table");
            //System.out.println(mapper.writeValueAsString(ds));
            geoserver.createDBDatastore(ds, NAMESPACE);
            LOG.info("Added store:", ds);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding store");
        }

        // for data modification (WFS) - data
        try {
            FeatureType featureData = new FeatureType();
            featureData.enabled = true;
            featureData.name = "vuser_layer_data";
            featureData.srs = srs;

            geoserver.createFeatureType(featureData, NAMESPACE, storeName);
            LOG.info("Added featuretype:", featureData);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding featuretype vuser_layer_data");
        }

        // for viewing (WMS) - combination for data/style
        FeatureType featureStyledData = new FeatureType();
        try {
            featureStyledData.enabled = true;
            featureStyledData.name = "user_layer_data_style";
            featureStyledData.srs = srs;

            geoserver.createFeatureType(featureStyledData, NAMESPACE, storeName);
            LOG.info("Added featuretype:", featureStyledData);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding featuretype user_layer_data_style");
        }

        final String sld = IOHelper.readString(GeoserverPopulator.class.getResourceAsStream("/sld/userlayer/UserLayerDefaultStyle.sld"));
        final String sldName = "UserLayerDefaultStyle";
        try {
            geoserver.createSLD(sldName, sld);
            LOG.info("Added SLD:", sldName);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding SLD");
        }
        try {
            geoserver.linkStyleToLayer(sldName, featureStyledData.name, NAMESPACE);
            LOG.info("Linked SLD", sldName, " to layer", featureStyledData.name, "in namespace", NAMESPACE);
        } catch (FeignException ex) {
            LOG.error(ex, "Error linking SLD");
        }
        geoserver.setDefaultStyleForLayer(sldName, featureStyledData.name, NAMESPACE);
        LOG.info("Set default SLD", sldName, " for layer", featureStyledData.name, "in namespace", NAMESPACE);
    }
}
