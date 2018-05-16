package org.oskari.map.userlayer.input;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.ogr.OGRDataStoreFactory;
import org.geotools.data.ogr.bridj.BridjOGRDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;

public class GPXParser implements FeatureCollectionParser {

    private static final Logger LOG = LogFactory.getLogger(KMLParser.class);

    public static final String SUFFIX = "GPX";

    private static final String[] TYPENAMES = {
            "tracks",
            "routes",
            "waypoints"
    };

    @Override
    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws ServiceException {
        OGRDataStoreFactory factory = new BridjOGRDataStoreFactory();
        if (!factory.isAvailable()) {
            throw new ServiceException("GDAL library is not found for GPX import");
        }

        try {
            // GPX always lon,lat 4326
            sourceCRS = CRS.decode("EPSG:4326", true);
        } catch (FactoryException e) {
            throw new ServiceException("Failed to decode EPSG:4326");
        }

        Map<String, String> connectionParams = new HashMap<>();
        connectionParams.put("DriverName", "GPX");
        connectionParams.put("DatasourceName", file.getAbsolutePath());

        DataStore store = null;
        try {
            store = factory.createDataStore(connectionParams);
            String[] storeTypeNames = store.getTypeNames();
            LOG.debug("gpx typeNames {}", Arrays.toString(storeTypeNames));
            for (String typeName : TYPENAMES) {
                if (!contains(storeTypeNames, typeName)) {
                    continue;
                }
                SimpleFeatureSource source = store.getFeatureSource(typeName);
                SimpleFeatureCollection collection = FeatureCollectionParsers.read(source, sourceCRS, targetCRS);
                if (collection.isEmpty()) {
                    LOG.info("FeatureCollection was empty, typeName {}", typeName);
                } else {
                    return collection;
                }
            }
            throw new ServiceException("Could not find any un-empty FeatureCollections from GPX file");
        } catch (Exception e) {
            throw new ServiceException("GPX parsing failed", e);
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    private boolean contains(String[] a, String k) {
        for (String s : a) {
            if (s.equals(k)) {
                return true;
            }
        }
        return false;
    }
}
