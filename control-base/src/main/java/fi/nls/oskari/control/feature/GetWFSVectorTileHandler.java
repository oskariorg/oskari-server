package fi.nls.oskari.control.feature;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.oskari.service.mvt.SimpleFeaturesMVTEncoder;
import org.oskari.service.util.ServiceFactory;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ResponseHelper;
import net.opengis.wfs.FeatureCollectionType;

@OskariActionRoute("GetWFSVectorTile")
public class GetWFSVectorTileHandler extends ActionHandler {

    protected static final String MVT_CONTENT_TYPE = "application/vnd.mapbox-vector-tile";
    protected static final String PARAM_BBOX = "bbox";

    private final Cache<byte[]> cache = new Cache<>();
    private OskariLayerService layerService;

    public void setLayerService(OskariLayerService layerService) {
        this.layerService = layerService;
    }

    @Override
    public void init() {
        if (layerService == null) {
            layerService = ServiceFactory.getMapLayerService();
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        int layerId = params.getRequiredParamInt(ActionConstants.PARAM_ID);
        OskariLayer layer = getLayer(layerId);

        String bboxStr = params.getRequiredParam(PARAM_BBOX);
        double[] bbox = parseBbox(bboxStr);

        String srs = params.getRequiredParam(ActionConstants.PARAM_SRS);

        String cacheKey = getCacheKey(layerId, bboxStr, srs);
        byte[] cached =  cache.get(cacheKey);
        if (cached != null) {
            ResponseHelper.writeResponse(params, 200, MVT_CONTENT_TYPE, cached);
            return;
        }

        SimpleFeatureCollection sfc = getFeatures(layer, bbox, srs);
        byte[] encoded = SimpleFeaturesMVTEncoder.encodeToByteArray(sfc, layer.getName(), bbox, 4096, 256);
        cache.put(cacheKey, encoded);
        params.getResponse().addHeader("Access-Control-Allow-Origin", "*");
        ResponseHelper.writeResponse(params, 200, MVT_CONTENT_TYPE, encoded);
    }

    private String getCacheKey(int layerId, String bboxStr, String srs) {
        return "WFS_" + layerId + "_" + bboxStr + "_" + srs;
    }

    protected OskariLayer getLayer(int layerId) throws ActionParamsException {
        OskariLayer layer = layerService.find(layerId);
        if (layer == null) {
            throw new ActionParamsException("Unknown layerId");
        }
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            throw new ActionParamsException("Specified layer is not a WFS layer");
        }
        return layer;
    }

    protected double[] parseBbox(String str) throws ActionParamsException {
        String[] coordinates = str.split(",");
        if (coordinates.length != 4) {
            throw new ActionParamsException("Invalid value for key: " + PARAM_BBOX);
        }
        try {
            double[] bbox = new double[4];
            for (int i = 0; i < 4; i++) {
                bbox[i] = Double.parseDouble(coordinates[i]);
            }
            return bbox;
        } catch (NumberFormatException e) {
            throw new ActionParamsException("Invalid value for key: " + PARAM_BBOX);
        }
    }

    private SimpleFeatureCollection getFeatures(OskariLayer layer, double[] bbox, String srsName) throws ActionException {
        String endPoint = layer.getUrl();
        String version = layer.getVersion();
        String typeName = layer.getName();

        String getFeatureKVP = getFeature(endPoint, version, typeName, bbox, srsName, 10000);

        OskariGMLConfiguration cfg = new OskariGMLConfiguration(layer.getUsername(), layer.getPassword());
        Parser parser = getParser(cfg);

        try {
            HttpURLConnection conn = IOHelper.getConnection(getFeatureKVP, layer.getUsername(), layer.getPassword());
            byte[] response = IOHelper.readBytes(conn);
            Object result = parser.parse(new ByteArrayInputStream(response));
            return toFeatureCollection(result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ActionException("Failed to read feature collection from WFS service!", e);
        }
    }

    private String getFeature(String endPoint, String version, String typeName, double[] bbox, String srsName, int maxFeatures) {
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

    private String getBBOX(String version, double[] bbox, String srsName) {
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

    private Parser getParser(OskariGMLConfiguration cfg) {
        Parser parser = new Parser(cfg);
        parser.setValidating(false);
        parser.setFailOnValidationError(false);
        parser.setStrict(false);
        return parser;
    }

    private SimpleFeatureCollection toFeatureCollection(Object obj) {
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
