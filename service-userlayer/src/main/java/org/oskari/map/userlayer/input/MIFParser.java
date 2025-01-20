package org.oskari.map.userlayer.input;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.map.userlayer.service.UserLayerException;

import fi.nls.oskari.service.ServiceException;

/**
 * Parse MapInfo MIF/MID
 */
public class MIFParser implements FeatureCollectionParser {

    public static final String SUFFIX = "MIF";

    @Override
    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws ServiceException {
        URL url;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new UserLayerException("Failed to parse MIF: " + e.getMessage(),
                    UserLayerException.ErrorType.PARSER);
        }

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", url);

        DataStore store = null;
        try {
            store = DataStoreFinder.getDataStore(params);
            String typeName = store.getTypeNames()[0];
            SimpleFeatureSource source = store.getFeatureSource(typeName);
            CoordinateReferenceSystem crs = source.getSchema()
                    .getGeometryDescriptor()
                    .getCoordinateReferenceSystem();
            if (crs != null) {
                sourceCRS = crs;
            }
            return FeatureCollectionParsers.read(source, sourceCRS, targetCRS);
        } catch (ServiceException e) {
            // forward error on read: if in file UserLayerException. if in service ServiceException
            throw e;
        } catch (Exception e) {
            throw new UserLayerException("Failed to parse MIF: " + e.getMessage(),
                    UserLayerException.ErrorType.PARSER, UserLayerException.ErrorType.INVALID_FORMAT);
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    @Override
    public String getSuffix() {
        return SUFFIX;
    }

}
