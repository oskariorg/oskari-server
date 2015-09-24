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
public class MyplacesHelper {

    public static final String MODULE_NAME = "myplaces";

    private static final Logger LOG = LogFactory.getLogger(MyplacesHelper.class);

    public static void setupMyplaces(final String srs) throws Exception {
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
            // in 2.5.2 namespace = NAMESPACE, in 2.7.1 it needs to be the uri?
            ds.connectionParameters.namespace = ns.uri;
            ds.addEntry("Loose bbox", "true");
            //System.out.println(mapper.writeValueAsString(ds));
            geoserver.createDBDatastore(ds, GeoserverPopulator.NAMESPACE);
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

            geoserver.createFeatureType(featureCategories, GeoserverPopulator.NAMESPACE, storeName);
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

            geoserver.createFeatureType(featurePlaces, GeoserverPopulator.NAMESPACE, storeName);
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

            geoserver.createFeatureType(featurePlacesCategories, GeoserverPopulator.NAMESPACE, storeName);
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
            geoserver.linkStyleToLayer(sldName, featurePlacesCategories.name, GeoserverPopulator.NAMESPACE);
            LOG.info("Linked SLD", sldName, " to layer", featurePlacesCategories.name, "in namespace", GeoserverPopulator.NAMESPACE);
        } catch (FeignException ex) {
            LOG.error(ex, "Error linking SLD");
        }
        geoserver.setDefaultStyleForLayer(sldName, featurePlacesCategories.name, GeoserverPopulator.NAMESPACE);
        LOG.info("Set default SLD", sldName, " for layer", featurePlacesCategories.name, "in namespace", GeoserverPopulator.NAMESPACE);
    }
}
