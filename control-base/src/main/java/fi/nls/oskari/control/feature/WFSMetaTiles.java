package fi.nls.oskari.control.feature;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import net.opengis.wfs.FeatureCollectionType;

public class WFSMetaTiles {
    
    private static final Logger LOG = LogFactory.getLogger(WFSMetaTiles.class);

    // Only cache 100 metatiles
    private static final int LIMIT = 100;
    private static final ComputeOnceCache<SimpleFeatureCollection> CACHE = new ComputeOnceCache<>(LIMIT);
    
    public static SimpleFeatureCollection getFeatures(OskariLayer layer, String srs, WFSTileGrid grid, TileCoord tile)
            throws ActionException {
        String endPoint = layer.getUrl();
        String version = layer.getVersion();
        String typeName = layer.getName();
        double[] bbox = grid.getTileExtent(tile);

        final String getFeatureKVP = getFeature(endPoint, version, typeName, bbox, srs, 10000);
        final String user = layer.getUsername();
        final String pass = layer.getPassword();
        
        // Use the whole getFeatureKVP request as the cache key
        final SimpleFeatureCollection sfc = CACHE.get(getFeatureKVP, (String k) -> {
            try {
                OskariGMLConfiguration cfg = new OskariGMLConfiguration(user, pass);
                Parser parser = getParser(cfg);
                HttpURLConnection conn = IOHelper.getConnection(getFeatureKVP, user, pass);
                byte[] response = IOHelper.readBytes(conn);
                Object result = parser.parse(new ByteArrayInputStream(response));
                return toFeatureCollection(result);
            } catch (Exception e) {
                LOG.warn(e, "Failed to load features from url:", getFeatureKVP);
                return null;
            }
        });

        if (sfc == null) {
            throw new ActionException("Failed to read features!");
        }
        return sfc;
    }
    
    private static String getFeature(String endPoint, String version, String typeName, double[] bbox, String srsName, int maxFeatures) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("SERVICE", "WFS");
        parameters.put("VERSION", version);
        parameters.put("REQUEST", "GetFeature");
        parameters.put("TYPENAME", typeName);
        parameters.put("BBOX", getBBOX(version, bbox, srsName));
        parameters.put("SRSNAME", srsName);
        parameters.put("MAXFEATURES", Integer.toString(maxFeatures));
        return IOHelper.constructUrl(endPoint, parameters);
    }

    private static String getBBOX(String version, double[] bbox, String srsName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(bbox[i]);
            sb.append(',');
        }
        if ("1.1.0".equals(version)) {
            sb.append(srsName);
            return sb.toString();
        } else {
            return sb.substring(0, sb.length() - 1);
        }
    }

    private static Parser getParser(OskariGMLConfiguration cfg) {
        Parser parser = new Parser(cfg);
        parser.setValidating(false);
        parser.setFailOnValidationError(false);
        parser.setStrict(false);
        return parser;
    }

    private static SimpleFeatureCollection toFeatureCollection(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof SimpleFeatureCollection) {
            return (SimpleFeatureCollection) obj;
        }
        if (obj instanceof SimpleFeature) {
            SimpleFeature feature = (SimpleFeature) obj;
            return DataUtilities.collection(feature);
        }
        if (obj instanceof FeatureCollectionType) {
            FeatureCollectionType collectionType = (FeatureCollectionType) obj;
            for (Object entry : collectionType.getFeature()) {
                SimpleFeatureCollection collection = toFeatureCollection(entry);
                if (entry != null) {
                    return collection;
                }
            }
            return null;
        } else {
            throw new ClassCastException(obj.getClass()
                    + " produced when FeatureCollection expected"
                    + " check schema use of AbstractFeatureCollection");
        }
    }

}
