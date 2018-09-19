package fi.nls.oskari.control.feature;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
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
    private static final FeatureJSON FJ = new FeatureJSON();

    // Only cache 100 metatiles
    private static final int LIMIT = 100;
    private static final ComputeOnceCache<SimpleFeatureCollection> CACHE = new ComputeOnceCache<>(LIMIT);

    public static SimpleFeatureCollection getFeatures(OskariLayer layer, String srs, WFSTileGrid grid, TileCoord tile)
            throws ActionException {
        String endPoint = layer.getUrl();
        String version = layer.getVersion();
        String typeName = layer.getName();
        double[] bbox = grid.getTileExtent(tile);

        final String getFeatureKVP = getFeatureKVP(endPoint, version, typeName, bbox, srs, 10000);
        final String user = layer.getUsername();
        final String pass = layer.getPassword();

        // Use the whole getFeatureKVP request as the cache key
        final SimpleFeatureCollection sfc = CACHE.get(getFeatureKVP, (String k) -> {
            // First try GeoJSON (faster and easier to write and read)
            SimpleFeatureCollection fc = getFeatureGeoJSON(getFeatureKVP, user, pass);
            return fc != null ? fc : getFeatureGML(getFeatureKVP, user, pass);
        });

        if (sfc == null) {
            throw new ActionException("Failed to read features!");
        }
        return sfc;
    }

    private static String getFeatureKVP(String endPoint, String version, String typeName, double[] bbox, String srsName, int maxFeatures) {
        Map<String, String> parameters = new LinkedHashMap<>();
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

    private static SimpleFeatureCollection getFeatureGeoJSON(String getFeatureKVP, String user, String pass) {
        try {
            String request = getFeatureKVP + "&outputFormat=application/json";
            HttpURLConnection conn = IOHelper.getConnection(request, user, pass);
            boolean json = conn.getContentType().contains("json");
            if (!json) {
                IOHelper.readBytes(conn);
                return null;
            }
            boolean gzip = "gzip".equals(conn.getContentType());
            try (InputStream in = new BufferedInputStream(gzip ? new GZIPInputStream(conn.getInputStream()) : conn.getInputStream())) {
                DefaultFeatureCollection features = new DefaultFeatureCollection(null, null);
                FeatureIterator<SimpleFeature> it = FJ.streamFeatureCollection(in);
                while (it.hasNext()) {
                    features.add(it.next());
                }
                return features;
            }
        } catch (IOException e) {
            LOG.info(e, "Failed to read as GeoJSON");
            return null;
        }
    }

    private static SimpleFeatureCollection getFeatureGML(String getFeatureKVP, String user, String pass) {
        try {
            OskariGMLConfiguration cfg = new OskariGMLConfiguration(user, pass);
            Parser parser = getParser(cfg);
            HttpURLConnection conn = IOHelper.getConnection(getFeatureKVP, user, pass);
            byte[] response = IOHelper.readBytes(conn);
            Object result = parser.parse(new ByteArrayInputStream(response));
            return toFeatureCollection(result);
        } catch (Exception e) {
            LOG.warn(e, "Failed to read as GML");
            return null;
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
