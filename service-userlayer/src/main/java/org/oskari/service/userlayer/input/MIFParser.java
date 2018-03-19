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
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.service.ServiceException;

/**
 * Parse MapInfo MIF/MID 
 */
public class MIFParser implements FeatureCollectionParser {

    public static final String SUFFIX = "MIF";
    
    private CoordinateReferenceSystem crs;

    @Override
    public SimpleFeatureCollection parse(File file) throws ServiceException {
        OGRDataStoreFactory factory = new BridjOGRDataStoreFactory();

        Map<String, String> connectionParams = new HashMap<String, String>();
        connectionParams.put("DriverName", "MapInfo File");
        connectionParams.put("DatasourceName", file.getAbsolutePath());

        DataStore store = null;
        try {
            store = factory.createDataStore(connectionParams);
            String typeName = store.getTypeNames()[0];
            SimpleFeatureSource source = store.getFeatureSource(typeName);
            SimpleFeatureType schema = source.getSchema();
            crs = schema.getGeometryDescriptor().getCoordinateReferenceSystem();
            return source.getFeatures();
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
        return crs;
    }

}
