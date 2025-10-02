package org.oskari.map.myfeatures.input;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.oskari.map.myfeatures.service.MyFeaturesException;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.service.ServiceException;

public class GPKGParser implements FeatureCollectionParser {
    
    public static final String SUFFIX = "GPKG";

    @Override
    public String getSuffix() {
        return SUFFIX;
    }

    @Override
    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws ServiceException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("dbtype", "geopkg");
        params.put("database", file);

        DataStore store = null;
        try {
            store = DataStoreFinder.getDataStore(params);
            for (String typeName : store.getTypeNames()) {
                SimpleFeatureSource source = store.getFeatureSource(typeName);
                CoordinateReferenceSystem crs = source.getSchema()
                        .getGeometryDescriptor()
                        .getCoordinateReferenceSystem();
                if (crs != null) {
                    sourceCRS = crs;
                }
                SimpleFeatureCollection collection = FeatureCollectionParsers.read(source, sourceCRS, targetCRS);
                if (!collection.isEmpty()) {
                    return collection;
                }
            }
            throw new MyFeaturesException("Failed to parse GPKG: Could not find non-empty feature collection",
                    MyFeaturesException.ErrorType.NO_FEATURES);
        } catch (ServiceException e) {
            // forward error on read: if in file UserLayerException. if in service ServiceException
            throw e;
        } catch (Exception e) {
            throw new MyFeaturesException("Failed to parse GPKG: " + e.getMessage(),
                    MyFeaturesException.ErrorType.PARSER, MyFeaturesException.ErrorType.INVALID_FORMAT);
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

}
