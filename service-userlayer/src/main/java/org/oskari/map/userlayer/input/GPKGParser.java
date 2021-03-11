package org.oskari.map.userlayer.input;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.map.userlayer.service.UserLayerException;

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
        SimpleFeatureCollection collection = null;
        try {
            store = DataStoreFinder.getDataStore(params);
            for (String typeName : store.getTypeNames()) {
                SimpleFeatureSource source = store.getFeatureSource(typeName);
                SimpleFeatureType sft = store.getSchema(typeName);
                CoordinateReferenceSystem crs = sft
                        .getGeometryDescriptor()
                        .getCoordinateReferenceSystem();
                if (crs != null) {
                    sourceCRS = crs;
                }
                collection = FeatureCollectionParsers.read(source, sourceCRS, targetCRS);
                if (!collection.isEmpty()) {
                    break;
                }
            }
            return collection;
        } catch (ServiceException e) {
            // forward error on read: if in file UserLayerException. if in service ServiceException
            throw e;
        } catch (Exception e) {
            throw new UserLayerException("Failed to parse GPKG: " + e.getMessage(),
                    UserLayerException.ErrorType.PARSER, UserLayerException.ErrorType.INVALID_FORMAT);
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

}
