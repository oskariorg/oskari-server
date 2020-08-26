package org.oskari.usercontent;

import feign.FeignException;
import fi.nls.oskari.db.ConnectionInfo;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;


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
            ds.connectionParameters.host = info.getHost();
            ds.connectionParameters.port = info.getPort();
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
            GeoserverPopulator.resolveCRS(featureData, srs);

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
            GeoserverPopulator.resolveCRS(featureStyledData, srs);

            geoserver.createFeatureType(featureStyledData, GeoserverPopulator.NAMESPACE, storeName);
            LOG.info("Added featuretype:", featureStyledData);
        } catch (FeignException ex) {
            LOG.error(ex, "Error adding featuretype analysis_data_style");
        }
    }
}
