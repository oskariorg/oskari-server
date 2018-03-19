package org.oskari.service.userlayer.input;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.ogr.OGRDataStoreFactory;
import org.geotools.data.ogr.bridj.BridjOGRDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.service.ServiceException;

public class GPXParser implements FeatureCollectionParser {

    public static final String SUFFIX = "GPX";

    @Override
    public SimpleFeatureCollection parse(File file) throws ServiceException {
        OGRDataStoreFactory factory = new BridjOGRDataStoreFactory();
        if (!factory.isAvailable()) {
            throw new ServiceException("GDAL library is not found for GPX import");
        }

        Map<String, String> connectionParams = new HashMap<>();
        connectionParams.put("DriverName", "GPX");
        connectionParams.put("DatasourceName", file.getAbsolutePath());
        
        DataStore store = null;
        try {
            store = factory.createDataStore(connectionParams);
            for (String typeName : store.getTypeNames()) {
                // Skip track points
                if ("track_points".equals(typeName)) {
                    continue;
                }
                SimpleFeatureSource source = store.getFeatureSource(typeName);
                return source.getFeatures();
            }
            throw new ServiceException("Could not find any usable typeNames from GPX file");
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    @Override
    public CoordinateReferenceSystem getDeterminedProjection() {
        try {
            // GPX is always 4326 lon,lat
            return CRS.decode("EPSG:4326", true);
        } catch (Exception ignore) {
            return null;
        }
    }
}
