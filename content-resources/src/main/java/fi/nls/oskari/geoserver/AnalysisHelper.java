package fi.nls.oskari.geoserver;

import feign.FeignException;
import fi.nls.oskari.db.ConnectionInfo;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;


/**
 * Created by SMAKINEN on 4.9.2015.
 */
public class AnalysisHelper {

    public static final String MODULE_NAME = "analysis";

    private static final Logger LOG = LogFactory.getLogger(AnalysisHelper.class);

    public static void setupAnalysis(final String srs)
            throws Exception {
        Geoserver geoserver = GeoserverPopulator.getGeoserver(MODULE_NAME);

        // Creating a namespace creates a workspace
        // (with ws you can only give a name, with ns you can also provide the uri)
        Namespace ns = new Namespace();
        try {

            ns.prefix = GeoserverPopulator.NAMESPACE;
            ns.uri = "http://www.oskari.org";
            geoserver.createNamespace(ns);
            LOG.info("Added namespace:", ns);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding namespace");
        }

        final String storeName = MODULE_NAME;
        try {
            DBDatastore ds = new DBDatastore();
            ds.name = storeName;

            DatasourceHelper helper = DatasourceHelper.getInstance();
            ConnectionInfo info = helper.getPropsForDS(MODULE_NAME);

            ds.connectionParameters.user = info.user;
            ds.connectionParameters.passwd = info.pass;
            ds.connectionParameters.database = info.getDBName();
            // !! in 2.5.2 namespace = NS PREFIX, in 2.7.1 it needs to be the NS URI!!
            ds.connectionParameters.namespace = ns.uri;
            ds.addEntry("Loose bbox", "true");
            //System.out.println(mapper.writeValueAsString(ds));
            geoserver.createDBDatastore(ds, GeoserverPopulator.NAMESPACE);
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

            geoserver.createFeatureType(featureData, GeoserverPopulator.NAMESPACE, storeName);
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

            geoserver.createFeatureType(featureStyledData, GeoserverPopulator.NAMESPACE, storeName);
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
            geoserver.linkStyleToLayer(sldName, featureStyledData.name, GeoserverPopulator.NAMESPACE);
            LOG.info("Linked SLD", sldName, " to layer", featureStyledData.name, "in namespace", GeoserverPopulator.NAMESPACE);
        } catch (FeignException ex) {
            LOG.error(ex, "Error linking SLD");
        }
        geoserver.setDefaultStyleForLayer(sldName, featureStyledData.name, GeoserverPopulator.NAMESPACE);
        LOG.info("Set default SLD", sldName, " for layer", featureStyledData.name, "in namespace", GeoserverPopulator.NAMESPACE);
    }
}
